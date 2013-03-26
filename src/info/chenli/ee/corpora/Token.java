

/* First created by JCasGen Wed Mar 06 00:08:51 GMT+08:00 2013 */
package info.chenli.ee.corpora;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Mar 26 13:33:27 GMT 2013
 * XML source: /Users/chenli/projects/bionlp2013/eventExtractor/desc/typeSystemDescriptor.xml
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

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: pos

  /** getter for pos - gets 
   * @generated */
  public String getPos() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "info.chenli.ee.corpora.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_pos);}
    
  /** setter for pos - sets  
   * @generated */
  public void setPos(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "info.chenli.ee.corpora.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_pos, v);}    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated */
  public String getLemma() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "info.chenli.ee.corpora.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_lemma);}
    
  /** setter for lemma - sets  
   * @generated */
  public void setLemma(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "info.chenli.ee.corpora.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_lemma, v);}    
   
    
  //*--------------*
  //* Feature: stem

  /** getter for stem - gets 
   * @generated */
  public String getStem() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "info.chenli.ee.corpora.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_stem);}
    
  /** setter for stem - sets  
   * @generated */
  public void setStem(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "info.chenli.ee.corpora.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_stem, v);}    
   
    
  //*--------------*
  //* Feature: leftToken

  /** getter for leftToken - gets 
   * @generated */
  public Token getLeftToken() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_leftToken == null)
      jcasType.jcas.throwFeatMissing("leftToken", "info.chenli.ee.corpora.Token");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_leftToken)));}
    
  /** setter for leftToken - sets  
   * @generated */
  public void setLeftToken(Token v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_leftToken == null)
      jcasType.jcas.throwFeatMissing("leftToken", "info.chenli.ee.corpora.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_leftToken, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: rightToken

  /** getter for rightToken - gets 
   * @generated */
  public Token getRightToken() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_rightToken == null)
      jcasType.jcas.throwFeatMissing("rightToken", "info.chenli.ee.corpora.Token");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_rightToken)));}
    
  /** setter for rightToken - sets  
   * @generated */
  public void setRightToken(Token v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_rightToken == null)
      jcasType.jcas.throwFeatMissing("rightToken", "info.chenli.ee.corpora.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_rightToken, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: dependentId

  /** getter for dependentId - gets 
   * @generated */
  public int getDependentId() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_dependentId == null)
      jcasType.jcas.throwFeatMissing("dependentId", "info.chenli.ee.corpora.Token");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Token_Type)jcasType).casFeatCode_dependentId);}
    
  /** setter for dependentId - sets  
   * @generated */
  public void setDependentId(int v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_dependentId == null)
      jcasType.jcas.throwFeatMissing("dependentId", "info.chenli.ee.corpora.Token");
    jcasType.ll_cas.ll_setIntValue(addr, ((Token_Type)jcasType).casFeatCode_dependentId, v);}    
   
    
  //*--------------*
  //* Feature: dependent

  /** getter for dependent - gets 
   * @generated */
  public Token getDependent() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_dependent == null)
      jcasType.jcas.throwFeatMissing("dependent", "info.chenli.ee.corpora.Token");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_dependent)));}
    
  /** setter for dependent - sets  
   * @generated */
  public void setDependent(Token v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_dependent == null)
      jcasType.jcas.throwFeatMissing("dependent", "info.chenli.ee.corpora.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_dependent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: relation

  /** getter for relation - gets 
   * @generated */
  public String getRelation() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_relation == null)
      jcasType.jcas.throwFeatMissing("relation", "info.chenli.ee.corpora.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_relation);}
    
  /** setter for relation - sets  
   * @generated */
  public void setRelation(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_relation == null)
      jcasType.jcas.throwFeatMissing("relation", "info.chenli.ee.corpora.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_relation, v);}    
                                      //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated */
  public int getId() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Token");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Token_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(int v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Token");
    jcasType.ll_cas.ll_setIntValue(addr, ((Token_Type)jcasType).casFeatCode_id, v);}    
   
    
}

    