

/* First created by JCasGen Tue Mar 05 17:56:53 GMT+08:00 2013 */
package info.chenli.litway.corpora;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Apr 11 16:40:07 BST 2013
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
     
  //*--------------*
  //* Feature: EventType

  /** getter for EventType - gets 
   * @generated */
  public String getEventType() {
    if (Trigger_Type.featOkTst && ((Trigger_Type)jcasType).casFeat_eventType == null)
      jcasType.jcas.throwFeatMissing("eventType", "info.chenli.litway.corpora.Trigger");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Trigger_Type)jcasType).casFeatCode_eventType);}
    
  /** setter for EventType - sets  
   * @generated */
  public void setEventType(String v) {
    if (Trigger_Type.featOkTst && ((Trigger_Type)jcasType).casFeat_eventType == null)
      jcasType.jcas.throwFeatMissing("eventType", "info.chenli.litway.corpora.Trigger");
    jcasType.ll_cas.ll_setStringValue(addr, ((Trigger_Type)jcasType).casFeatCode_eventType, v);}    
   
    
  //*--------------*
  //* Feature: EventType2

  /** getter for EventType2 - gets 
   * @generated */
  public String getEventType2() {
    if (Trigger_Type.featOkTst && ((Trigger_Type)jcasType).casFeat_eventType2 == null)
      jcasType.jcas.throwFeatMissing("eventType2", "info.chenli.litway.corpora.Trigger");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Trigger_Type)jcasType).casFeatCode_eventType2);}
    
  /** setter for EventType2 - sets  
   * @generated */
  public void setEventType2(String v) {
    if (Trigger_Type.featOkTst && ((Trigger_Type)jcasType).casFeat_eventType2 == null)
      jcasType.jcas.throwFeatMissing("eventType2", "info.chenli.litway.corpora.Trigger");
    jcasType.ll_cas.ll_setStringValue(addr, ((Trigger_Type)jcasType).casFeatCode_eventType2, v);}    
                                  //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated */
  public String getId() {
    if (Trigger_Type.featOkTst && ((Trigger_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Trigger");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Trigger_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(String v) {
    if (Trigger_Type.featOkTst && ((Trigger_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Trigger");
    jcasType.ll_cas.ll_setStringValue(addr, ((Trigger_Type)jcasType).casFeatCode_id, v);}    
   
    
}

    