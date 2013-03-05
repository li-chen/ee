

/* First created by JCasGen Tue Mar 05 17:56:53 GMT+08:00 2013 */
package info.chenli.ee;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Mar 06 00:46:29 GMT+08:00 2013
 * XML source: /Users/chenli/projects/bionlp2013/eventExtractor/desc/typeSystemDescriptor.xml
 * @generated */
public class Trigger extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Trigger.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Trigger() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Trigger(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Trigger(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Trigger(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
}

    