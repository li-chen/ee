

/* First created by JCasGen Thu Apr 25 13:17:47 BST 2013 */
package info.chenli.litway.corpora;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.IntegerList;


/** 
 * Updated by JCasGen Fri Jan 30 10:19:46 CST 2015
 * XML source: /media/songrq/soft/litway/workspace/LitWay/desc/typeSystemDescriptor.xml
 * @generated */
public class Dependency extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Dependency.class);
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
  protected Dependency() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Dependency(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Dependency(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Dependency(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: sentenceId

  /** getter for sentenceId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSentenceId() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_sentenceId == null)
      jcasType.jcas.throwFeatMissing("sentenceId", "info.chenli.litway.corpora.Dependency");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Dependency_Type)jcasType).casFeatCode_sentenceId);}
    
  /** setter for sentenceId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSentenceId(String v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_sentenceId == null)
      jcasType.jcas.throwFeatMissing("sentenceId", "info.chenli.litway.corpora.Dependency");
    jcasType.ll_cas.ll_setStringValue(addr, ((Dependency_Type)jcasType).casFeatCode_sentenceId, v);}    
   
    
  //*--------------*
  //* Feature: heads

  /** getter for heads - gets 
   * @generated
   * @return value of the feature 
   */
  public IntegerList getHeads() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_heads == null)
      jcasType.jcas.throwFeatMissing("heads", "info.chenli.litway.corpora.Dependency");
    return (IntegerList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_heads)));}
    
  /** setter for heads - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setHeads(IntegerList v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_heads == null)
      jcasType.jcas.throwFeatMissing("heads", "info.chenli.litway.corpora.Dependency");
    jcasType.ll_cas.ll_setRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_heads, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: relations

  /** getter for relations - gets 
   * @generated
   * @return value of the feature 
   */
  public StringList getRelations() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_relations == null)
      jcasType.jcas.throwFeatMissing("relations", "info.chenli.litway.corpora.Dependency");
    return (StringList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_relations)));}
    
  /** setter for relations - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRelations(StringList v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_relations == null)
      jcasType.jcas.throwFeatMissing("relations", "info.chenli.litway.corpora.Dependency");
    jcasType.ll_cas.ll_setRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_relations, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: modifiers

  /** getter for modifiers - gets 
   * @generated
   * @return value of the feature 
   */
  public IntegerList getModifiers() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_modifiers == null)
      jcasType.jcas.throwFeatMissing("modifiers", "info.chenli.litway.corpora.Dependency");
    return (IntegerList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_modifiers)));}
    
  /** setter for modifiers - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setModifiers(IntegerList v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_modifiers == null)
      jcasType.jcas.throwFeatMissing("modifiers", "info.chenli.litway.corpora.Dependency");
    jcasType.ll_cas.ll_setRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_modifiers, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    