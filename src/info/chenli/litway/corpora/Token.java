

/* First created by JCasGen Wed Mar 06 00:08:51 GMT+08:00 2013 */
package info.chenli.litway.corpora;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Jan 30 10:19:46 CST 2015
 * XML source: /media/songrq/soft/litway/workspace/LitWay/desc/typeSystemDescriptor.xml
 * @generated */
public class Token extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Token.class);
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
  protected Token() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Token(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Token(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Token(JCas jcas, int begin, int end) {
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
  //* Feature: pos

  /** getter for pos - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPos() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "info.chenli.litway.corpora.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_pos);}
    
  /** setter for pos - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPos(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "info.chenli.litway.corpora.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_pos, v);}    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLemma() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "info.chenli.litway.corpora.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_lemma);}
    
  /** setter for lemma - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLemma(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "info.chenli.litway.corpora.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_lemma, v);}    
   
    
  //*--------------*
  //* Feature: stem

  /** getter for stem - gets 
   * @generated
   * @return value of the feature 
   */
  public String getStem() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "info.chenli.litway.corpora.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_stem);}
    
  /** setter for stem - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStem(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "info.chenli.litway.corpora.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_stem, v);}    
   
    
  //*--------------*
  //* Feature: subLemma

  /** getter for subLemma - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSubLemma() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_subLemma == null)
      jcasType.jcas.throwFeatMissing("subLemma", "info.chenli.litway.corpora.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_subLemma);}
    
  /** setter for subLemma - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSubLemma(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_subLemma == null)
      jcasType.jcas.throwFeatMissing("subLemma", "info.chenli.litway.corpora.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_subLemma, v);}    
   
    
  //*--------------*
  //* Feature: subStem

  /** getter for subStem - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSubStem() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_subStem == null)
      jcasType.jcas.throwFeatMissing("subStem", "info.chenli.litway.corpora.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_subStem);}
    
  /** setter for subStem - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSubStem(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_subStem == null)
      jcasType.jcas.throwFeatMissing("subStem", "info.chenli.litway.corpora.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_subStem, v);}    
   
    
  //*--------------*
  //* Feature: leftToken

  /** getter for leftToken - gets 
   * @generated
   * @return value of the feature 
   */
  public Token getLeftToken() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_leftToken == null)
      jcasType.jcas.throwFeatMissing("leftToken", "info.chenli.litway.corpora.Token");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_leftToken)));}
    
  /** setter for leftToken - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLeftToken(Token v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_leftToken == null)
      jcasType.jcas.throwFeatMissing("leftToken", "info.chenli.litway.corpora.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_leftToken, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: rightToken

  /** getter for rightToken - gets 
   * @generated
   * @return value of the feature 
   */
  public Token getRightToken() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_rightToken == null)
      jcasType.jcas.throwFeatMissing("rightToken", "info.chenli.litway.corpora.Token");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_rightToken)));}
    
  /** setter for rightToken - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRightToken(Token v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_rightToken == null)
      jcasType.jcas.throwFeatMissing("rightToken", "info.chenli.litway.corpora.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_rightToken, jcasType.ll_cas.ll_getFSRef(v));}    
    //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated
   * @return value of the feature 
   */
  public int getId() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Token");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Token_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setId(int v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Token");
    jcasType.ll_cas.ll_setIntValue(addr, ((Token_Type)jcasType).casFeatCode_id, v);}    
   
    
}

    