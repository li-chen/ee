

/* First created by JCasGen Tue Mar 05 19:15:26 GMT+08:00 2013 */
package info.chenli.litway.corpora;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


import org.apache.uima.jcas.cas.StringArray;


/** 
 * Updated by JCasGen Tue Mar 26 13:33:27 GMT 2013
 * XML source: /Users/chenli/projects/bionlp2013/eventExtractor/desc/typeSystemDescriptor.xml
 * @generated */
public class Event extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Event.class);
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
  protected Event() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Event(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Event(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Event(JCas jcas, int begin, int end) {
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
  //* Feature: Themes

  /** getter for Themes - gets 
   * @generated */
  public StringArray getThemes() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_themes == null)
      jcasType.jcas.throwFeatMissing("themes", "info.chenli.ee.corpora.Event");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Event_Type)jcasType).casFeatCode_themes)));}
    
  /** setter for Themes - sets  
   * @generated */
  public void setThemes(StringArray v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_themes == null)
      jcasType.jcas.throwFeatMissing("themes", "info.chenli.ee.corpora.Event");
    jcasType.ll_cas.ll_setRefValue(addr, ((Event_Type)jcasType).casFeatCode_themes, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for Themes - gets an indexed value - 
   * @generated */
  public String getThemes(int i) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_themes == null)
      jcasType.jcas.throwFeatMissing("themes", "info.chenli.ee.corpora.Event");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Event_Type)jcasType).casFeatCode_themes), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Event_Type)jcasType).casFeatCode_themes), i);}

  /** indexed setter for Themes - sets an indexed value - 
   * @generated */
  public void setThemes(int i, String v) { 
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_themes == null)
      jcasType.jcas.throwFeatMissing("themes", "info.chenli.ee.corpora.Event");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Event_Type)jcasType).casFeatCode_themes), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Event_Type)jcasType).casFeatCode_themes), i, v);}
   
    
  //*--------------*
  //* Feature: cause

  /** getter for cause - gets 
   * @generated */
  public String getCause() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_cause == null)
      jcasType.jcas.throwFeatMissing("cause", "info.chenli.ee.corpora.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_cause);}
    
  /** setter for cause - sets  
   * @generated */
  public void setCause(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_cause == null)
      jcasType.jcas.throwFeatMissing("cause", "info.chenli.ee.corpora.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_cause, v);}    
                //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated */
  public String getId() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Event");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Event_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(String v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Event");
    jcasType.ll_cas.ll_setStringValue(addr, ((Event_Type)jcasType).casFeatCode_id, v);}    
   
    
  //*--------------*
  //* Feature: trigger

  /** getter for trigger - gets 
   * @generated */
  public Trigger getTrigger() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_trigger == null)
      jcasType.jcas.throwFeatMissing("trigger", "info.chenli.ee.corpora.Event");
    return (Trigger)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Event_Type)jcasType).casFeatCode_trigger)));}
    
  /** setter for trigger - sets  
   * @generated */
  public void setTrigger(Trigger v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_trigger == null)
      jcasType.jcas.throwFeatMissing("trigger", "info.chenli.ee.corpora.Event");
    jcasType.ll_cas.ll_setRefValue(addr, ((Event_Type)jcasType).casFeatCode_trigger, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
}

    