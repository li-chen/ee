

/* First created by JCasGen Wed Apr 10 17:53:25 BST 2013 */
package info.chenli.litway.bionlp13;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Apr 11 16:40:07 BST 2013
 * XML source: /Users/chenli/projects/bionlp2013/eventExtractor/desc/typeSystemDescriptor.xml
 * @generated */
public class Complex extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Complex.class);
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
  protected Complex() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Complex(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Complex(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Complex(JCas jcas, int begin, int end) {
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
     
 
    
  //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated */
  public String getId() {
    if (Complex_Type.featOkTst && ((Complex_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.bionlp13.Complex");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Complex_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(String v) {
    if (Complex_Type.featOkTst && ((Complex_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.bionlp13.Complex");
    jcasType.ll_cas.ll_setStringValue(addr, ((Complex_Type)jcasType).casFeatCode_id, v);}    
  }

    