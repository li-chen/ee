package service;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import info.chenli.classifier.Accurary;

import java.io.*;
import java.util.*;

import service.*;

public class svm_predict {
	private static svm_print_interface svm_print_null = new svm_print_interface()
	{
		public void print(String s) {}
	};

	private static svm_print_interface svm_print_stdout = new svm_print_interface()
	{
		public void print(String s)
		{
			System.out.print(s);
		}
	};

	private static svm_print_interface svm_print_string = svm_print_stdout;

	static void info(String s) 
	{
		svm_print_string.print(s);
	}

	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	private static void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability) throws IOException
	{
		int correct = 0, tp = 0, tn = 0, fp = 0, fn = 0;
		int total = 0;
		double error = 0;
		float	p = 0, r = 0, f = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;

		if(predict_probability == 1)
		{
			if(svm_type == svm_parameter.EPSILON_SVR ||
			   svm_type == svm_parameter.NU_SVR)
			{
				svm_predict.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			}
			else
			{
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];
				output.writeBytes("labels");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(" "+labels[j]);
				output.writeBytes("\n");
			}
		}
		int ind = 1;
		File fnFile = new File("./trainfile/fn.trigger");
		File fpFile = new File("./trainfile/fp.trigger");
		File tokenFile = new File("./trainfile/trigger.devel.text.txt");
		OutputStreamWriter fnFileStream = new OutputStreamWriter(
				new FileOutputStream(fnFile), "UTF8");
		OutputStreamWriter fpFileStream = new OutputStreamWriter(
				new FileOutputStream(fpFile), "UTF8");
		InputStreamReader tokenFileStream = new InputStreamReader(
				new FileInputStream(tokenFile), "UTF8");		
		BufferedReader tokenFileBuffer = new BufferedReader(tokenFileStream);
		String triggerTextCh;
		Map<String,String> token = new HashMap<String,String>();
		while ((triggerTextCh = tokenFileBuffer.readLine()) != null) {
			String[] wordSb = triggerTextCh.split(" ");
			token.put(wordSb[0], wordSb[2]);
			}
		tokenFileBuffer.close();
		tokenFileStream.close();
		StringBuffer sbfn = new StringBuffer();
		StringBuffer sbfp = new StringBuffer();
		String ss[] = {	"Non_trigger", "Gene_expression", "Transcription", "Protein_catabolism", 
				"Localization", "Binding", "Protein_modification", "Phosphorylation", "Ubiquitination", "Acetylation"
				, "Deacetylation", "Regulation", "Positive_regulation", "Negative_regulation"};
		int  total14[] = new int [14];
		int  answer14[] = new int [14];
		int  correct14[] = new int [14];

		while(true)
		{
			String line = input.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			double target = atof(st.nextToken());
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			
			/*String[] ss = line.split("\t");
			double target = Double.valueOf(ss[0]);
			svm_node[] x = new svm_node[ss.length-1];
			for(int j=0;j<ss.length-1;j++)
			{
				x[j] = new svm_node();
				x[j].index = j+1;
				x[j].value = Double.valueOf(ss[j+1]);
			}*/

			double v;
			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
			{
				v = svm.svm_predict_probability(model,x,prob_estimates);
				output.writeBytes(v+" ");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(prob_estimates[j]+" ");
				output.writeBytes("\n");
			}
			else
			{
				v = svm.svm_predict(model,x);
				output.writeBytes(v+"\n");
			}

			if((int)v == (int)target) {
				++correct;
				for (int i=0; i<14; i++) {
					if ((int)target == i + 1) {
						correct14[i]++;
					}
				}
			}

			error += (v-target)*(v-target);
			sumv += v;
			sumy += target;
			sumvv += v*v;
			sumyy += target*target;
			sumvy += v*target;
			++total;
			for (int i=0; i<14; i++) {
				if ((int)target == i + 1) {
					total14[i]++;
				}
			}
			for (int i=0; i<14; i++) {
				if ((int)v == i + 1) {
					answer14[i]++;
				}
			}

			if((int)v != (int)target && (int)target != 1){
				fn++;
				sbfn.append(String.valueOf(ind) + "\t");
				sbfn.append(token.get(String.valueOf(ind)) + "\t");
				sbfn.append("answer:" + ss[(int)v - 1] + "\t");
				sbfn.append("gold:" + ss[(int)target - 1] + "\n");
				
			}
	
			if((int)v != (int)target && (int)v != 1){
				fp++;
				sbfp.append(String.valueOf(ind) + "\t");
				sbfp.append(token.get(String.valueOf(ind)) + "\t");
				sbfp.append("answer:" + ss[(int)v - 1] + "\t");
				sbfp.append("gold:" + ss[(int)target - 1] + "\n");
			}
		
			String fpStr = sbfp.toString();
			sbfp.delete(0,sbfp.length());
			String fnStr = sbfn.toString();
			sbfn.delete(0,sbfn.length());
			fpFileStream.write(fpStr);
			fnFileStream.write(fnStr);
			ind++;
		}
		fpFileStream.close();
		fnFileStream.close();

		tn = correct14[0];
		tp = correct - tn;
		p = (float) tp / (tp + fp);
		r = (float) tp / (tp + fn);
		f = (float) 2 * p * r / (p + r); 
		System.out.println();
		System.out.println(new Accurary(correct, total));
		System.out.println("tp: " + tp + "   fp: " + fp + "   fn: " + fn);
		System.out.println("p: " + p + "   r: " + r + "   f: " + f);
		for (int i=0; i<14; i++) {
			System.out.print(ss[i]);
			System.out.print("\t\t");
			System.out.print(total14[i]);
			System.out.print("\t\t");
			System.out.print(answer14[i]);
			System.out.print("\t\t");
			System.out.print(correct14[i]);
			System.out.print("\t\t");
			System.out.print((float)correct14[i]/total14[i]);
			System.out.print("\n");
		}
		
		if(svm_type == svm_parameter.EPSILON_SVR ||
		   svm_type == svm_parameter.NU_SVR)
		{
			svm_predict.info("Mean squared error = "+error/total+" (regression)\n");
			svm_predict.info("Squared correlation coefficient = "+
				 ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
				 ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
				 " (regression)\n");
		}
		else
			svm_predict.info("Accuracy = "+(double)correct/total*100+
				 "% ("+correct+"/"+total+") (classification)\n");
	}

	private static void exit_with_help()
	{
		System.err.print("usage: svm_predict [options] test_file model_file output_file\n"
		+"options:\n"
		+"-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n"
		+"-q : quiet mode (no outputs)\n");
		System.exit(1);
	}

	public static void main(String argv[]) throws IOException
	{
		int i, predict_probability=0;
        	svm_print_string = svm_print_stdout;

		// parse options
		for(i=0;i<argv.length;i++)
		{
			if(argv[i].charAt(0) != '-') break;
			++i;
			switch(argv[i-1].charAt(1))
			{
				case 'b':
					predict_probability = atoi(argv[i]);
					break;
				case 'q':
					svm_print_string = svm_print_null;
					i--;
					break;
				default:
					System.err.print("Unknown option: " + argv[i-1] + "\n");
					exit_with_help();
			}
		}
		if(i>=argv.length-2)
			exit_with_help();
		try 
		{
			BufferedReader input = new BufferedReader(new FileReader(argv[i]));
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i+2])));
			svm_model model = svm.svm_load_model(argv[i+1]);
			if (model == null)
			{
				System.err.print("can't open model file "+argv[i+1]+"\n");
				System.exit(1);
			}
			if(predict_probability == 1)
			{
				if(svm.svm_check_probability_model(model)==0)
				{
					System.err.print("Model does not support probabiliy estimates\n");
					System.exit(1);
				}
			}
			else
			{
				if(svm.svm_check_probability_model(model)!=0)
				{
					svm_predict.info("Model supports probability estimates, but disabled in prediction.\n");
				}
			}
			predict(input,output,model,predict_probability);
			input.close();
			output.close();
		} 
		catch(FileNotFoundException e) 
		{
			exit_with_help();
		}
		catch(ArrayIndexOutOfBoundsException e) 
		{
			exit_with_help();
		}
	}
}
