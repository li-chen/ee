
/* First created by JCasGen Wed Mar 06 00:08:51 GMT+08:00 2013 */
package info.chenli.ee.corpora;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Tue Mar 26 13:33:27 GMT 2013
 * @generated */
public class Token_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Token_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Token_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Token(addr, Token_Type.this);
  			   Token_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Token(addr, Token_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Token.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("info.chenli.ee.corpora.Token");
 
  /** @generated */
  final Feature casFeat_pos;
  /** @generated */
  final int     casFeatCode_pos;
  /** @generated */ 
  public String getPos(int addr) {
        if (featOkTst && casFeat_pos == null)
      jcas.throwFeatMissing("pos", "info.chenli.ee.corpora.Token");
    return ll_cas.ll_getStringValue(addr, casFeatCode_pos);
  }
  /** @generated */    
  public void setPos(int addr, String v) {
        if (featOkTst && casFeat_pos == null)
      jcas.throwFeatMissing("pos", "info.chenli.ee.corpora.Token");
    ll_cas.ll_setStringValue(addr, casFeatCode_pos, v);}
    
  
 
  /** @generated */
  final Feature casFeat_lemma;
  /** @generated */
  final int     casFeatCode_lemma;
  /** @generated */ 
  public String getLemma(int addr) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "info.chenli.ee.corpora.Token");
    return ll_cas.ll_getStringValue(addr, casFeatCode_lemma);
  }
  /** @generated */    
  public void setLemma(int addr, String v) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "info.chenli.ee.corpora.Token");
    ll_cas.ll_setStringValue(addr, casFeatCode_lemma, v);}
    
  
 
  /** @generated */
  final Feature casFeat_stem;
  /** @generated */
  final int     casFeatCode_stem;
  /** @generated */ 
  public String getStem(int addr) {
        if (featOkTst && casFeat_stem == null)
      jcas.throwFeatMissing("stem", "info.chenli.ee.corpora.Token");
    return ll_cas.ll_getStringValue(addr, casFeatCode_stem);
  }
  /** @generated */    
  public void setStem(int addr, String v) {
        if (featOkTst && casFeat_stem == null)
      jcas.throwFeatMissing("stem", "info.chenli.ee.corpora.Token");
    ll_cas.ll_setStringValue(addr, casFeatCode_stem, v);}
    
  
 
  /** @generated */
  final Feature casFeat_leftToken;
  /** @generated */
  final int     casFeatCode_leftToken;
  /** @generated */ 
  public int getLeftToken(int addr) {
        if (featOkTst && casFeat_leftToken == null)
      jcas.throwFeatMissing("leftToken", "info.chenli.ee.corpora.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_leftToken);
  }
  /** @generated */    
  public void setLeftToken(int addr, int v) {
        if (featOkTst && casFeat_leftToken == null)
      jcas.throwFeatMissing("leftToken", "info.chenli.ee.corpora.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_leftToken, v);}
    
  
 
  /** @generated */
  final Feature casFeat_rightToken;
  /** @generated */
  final int     casFeatCode_rightToken;
  /** @generated */ 
  public int getRightToken(int addr) {
        if (featOkTst && casFeat_rightToken == null)
      jcas.throwFeatMissing("rightToken", "info.chenli.ee.corpora.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_rightToken);
  }
  /** @generated */    
  public void setRightToken(int addr, int v) {
        if (featOkTst && casFeat_rightToken == null)
      jcas.throwFeatMissing("rightToken", "info.chenli.ee.corpora.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_rightToken, v);}
    
  
 
  /** @generated */
  final Feature casFeat_dependentId;
  /** @generated */
  final int     casFeatCode_dependentId;
  /** @generated */ 
  public int getDependentId(int addr) {
        if (featOkTst && casFeat_dependentId == null)
      jcas.throwFeatMissing("dependentId", "info.chenli.ee.corpora.Token");
    return ll_cas.ll_getIntValue(addr, casFeatCode_dependentId);
  }
  /** @generated */    
  public void setDependentId(int addr, int v) {
        if (featOkTst && casFeat_dependentId == null)
      jcas.throwFeatMissing("dependentId", "info.chenli.ee.corpora.Token");
    ll_cas.ll_setIntValue(addr, casFeatCode_dependentId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_dependent;
  /** @generated */
  final int     casFeatCode_dependent;
  /** @generated */ 
  public int getDependent(int addr) {
        if (featOkTst && casFeat_dependent == null)
      jcas.throwFeatMissing("dependent", "info.chenli.ee.corpora.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_dependent);
  }
  /** @generated */    
  public void setDependent(int addr, int v) {
        if (featOkTst && casFeat_dependent == null)
      jcas.throwFeatMissing("dependent", "info.chenli.ee.corpora.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_dependent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_relation;
  /** @generated */
  final int     casFeatCode_relation;
  /** @generated */ 
  public String getRelation(int addr) {
        if (featOkTst && casFeat_relation == null)
      jcas.throwFeatMissing("relation", "info.chenli.ee.corpora.Token");
    return ll_cas.ll_getStringValue(addr, casFeatCode_relation);
  }
  /** @generated */    
  public void setRelation(int addr, String v) {
        if (featOkTst && casFeat_relation == null)
      jcas.throwFeatMissing("relation", "info.chenli.ee.corpora.Token");
    ll_cas.ll_setStringValue(addr, casFeatCode_relation, v);}
    
  



  /** @generated */
  final Feature casFeat_id;
  /** @generated */
  final int     casFeatCode_id;
  /** @generated */ 
  public int getId(int addr) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Token");
    return ll_cas.ll_getIntValue(addr, casFeatCode_id);
  }
  /** @generated */    
  public void setId(int addr, int v) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Token");
    ll_cas.ll_setIntValue(addr, casFeatCode_id, v);}
    
  
 
  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Token_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_id = jcas.getRequiredFeatureDE(casType, "id", "uima.cas.Integer", featOkTst);
    casFeatCode_id  = (null == casFeat_id) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_id).getCode();

 
    casFeat_pos = jcas.getRequiredFeatureDE(casType, "pos", "uima.cas.String", featOkTst);
    casFeatCode_pos  = (null == casFeat_pos) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_pos).getCode();

 
    casFeat_lemma = jcas.getRequiredFeatureDE(casType, "lemma", "uima.cas.String", featOkTst);
    casFeatCode_lemma  = (null == casFeat_lemma) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_lemma).getCode();

 
    casFeat_stem = jcas.getRequiredFeatureDE(casType, "stem", "uima.cas.String", featOkTst);
    casFeatCode_stem  = (null == casFeat_stem) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_stem).getCode();

 
    casFeat_leftToken = jcas.getRequiredFeatureDE(casType, "leftToken", "info.chenli.ee.corpora.Token", featOkTst);
    casFeatCode_leftToken  = (null == casFeat_leftToken) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_leftToken).getCode();

 
    casFeat_rightToken = jcas.getRequiredFeatureDE(casType, "rightToken", "info.chenli.ee.corpora.Token", featOkTst);
    casFeatCode_rightToken  = (null == casFeat_rightToken) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_rightToken).getCode();

 
    casFeat_dependentId = jcas.getRequiredFeatureDE(casType, "dependentId", "uima.cas.Integer", featOkTst);
    casFeatCode_dependentId  = (null == casFeat_dependentId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_dependentId).getCode();

 
    casFeat_dependent = jcas.getRequiredFeatureDE(casType, "dependent", "info.chenli.ee.corpora.Token", featOkTst);
    casFeatCode_dependent  = (null == casFeat_dependent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_dependent).getCode();

 
    casFeat_relation = jcas.getRequiredFeatureDE(casType, "relation", "uima.cas.String", featOkTst);
    casFeatCode_relation  = (null == casFeat_relation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relation).getCode();

  }
}



    