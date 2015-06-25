package info.chenli.litway.bionlp13.ge;

import java.io.IOException;

import service.svm_train;
import service.svm_predict;

public class TokenRecognizer {
	public static void main(String[] args) {
		   String []arg ={ "-s", "0",
	                   "-t", "2",
	                   "-c", "32",
	                   "-g", "0.5",
	                   "-h", "1",
	                   "./model/instances.trigger.svm.txt", //存放SVM训练模型用的数据的路径
	                   "./model/triggers.model"//存放SVM通过训练数据训练出来的模型的路径

		   }; 
		   
		   String []parg={"./model/instances.trigger.svm.dev.txt", 
	                 "./model/triggers.model",
	                 "./model/trigger.out"}; 
	               
		   try {
			   System.out.println("........SVM运行开始.........."); 
			   svm_train.main(arg);
			   System.out.println("........SVM训练结束..........");
		       svm_predict.main(parg);  
			   System.out.println("........SVM运行结束.........."); 
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} 
		}
}
