

/* First created by JCasGen Wed Apr 10 18:09:04 BST 2013 */
package info.chenli.litway.bionlp13;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Apr 11 16:40:07 BST 2013
 * XML source: /Users/chenli/projects/bionlp2013/eventExtractor/desc/typeSystemDescriptor.xml
 * @generated */
public class CellularComponent extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(CellularComponent.class);
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
  protected CellularComponent() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public CellularComponent(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public CellularComponent(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public CellularComponent(JCas jcas, int begin, int end) {
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
    if (CellularComponent_Type.featOkTst && ((CellularComponent_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.bionlp13.CellularComponent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((CellularComponent_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(String v) {
    if (CellularComponent_Type.featOkTst && ((CellularComponent_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.bionlp13.CellularComponent");
    jcasType.ll_cas.ll_setStringValue(addr, ((CellularComponent_Type)jcasType).casFeatCode_id, v);}    
  }

    