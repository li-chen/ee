

/* First created by JCasGen Wed Apr 10 17:53:25 BST 2013 */
package info.chenli.litway.bionlp13;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Aug 08 16:36:25 BST 2013
 * XML source: /automount/isilon4_ifs-research/textmining/chenli/projects/bionlp/2013/eventExtractor/desc/typeSystemDescriptor.xml
 * @generated */
public class Chemical extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Chemical.class);
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
  protected Chemical() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Chemical(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Chemical(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Chemical(JCas jcas, int begin, int end) {
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
    if (Chemical_Type.featOkTst && ((Chemical_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.bionlp13.Chemical");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Chemical_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(String v) {
    if (Chemical_Type.featOkTst && ((Chemical_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.bionlp13.Chemical");
    jcasType.ll_cas.ll_setStringValue(addr, ((Chemical_Type)jcasType).casFeatCode_id, v);}    
  }

    