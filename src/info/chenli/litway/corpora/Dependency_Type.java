
/* First created by JCasGen Thu Apr 25 13:17:47 BST 2013 */
package info.chenli.litway.corpora;

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
 * Updated by JCasGen Fri Jan 30 10:19:46 CST 2015
 * @generated */
public class Dependency_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Dependency_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Dependency_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Dependency(addr, Dependency_Type.this);
  			   Dependency_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Dependency(addr, Dependency_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Dependency.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("info.chenli.litway.corpora.Dependency");
 
  /** @generated */
  final Feature casFeat_sentenceId;
  /** @generated */
  final int     casFeatCode_sentenceId;
  /** @generated */ 
  public String getSentenceId(int addr) {
        if (featOkTst && casFeat_sentenceId == null)
      jcas.throwFeatMissing("sentenceId", "info.chenli.litway.corpora.Dependency");
    return ll_cas.ll_getStringValue(addr, casFeatCode_sentenceId);
  }
  /** @generated */    
  public void setSentenceId(int addr, String v) {
        if (featOkTst && casFeat_sentenceId == null)
      jcas.throwFeatMissing("sentenceId", "info.chenli.litway.corpora.Dependency");
    ll_cas.ll_setStringValue(addr, casFeatCode_sentenceId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_heads;
  /** @generated */
  final int     casFeatCode_heads;
  /** @generated */ 
  public int getHeads(int addr) {
        if (featOkTst && casFeat_heads == null)
      jcas.throwFeatMissing("heads", "info.chenli.litway.corpora.Dependency");
    return ll_cas.ll_getRefValue(addr, casFeatCode_heads);
  }
  /** @generated */    
  public void setHeads(int addr, int v) {
        if (featOkTst && casFeat_heads == null)
      jcas.throwFeatMissing("heads", "info.chenli.litway.corpora.Dependency");
    ll_cas.ll_setRefValue(addr, casFeatCode_heads, v);}
    
  
 
  /** @generated */
  final Feature casFeat_relations;
  /** @generated */
  final int     casFeatCode_relations;
  /** @generated */ 
  public int getRelations(int addr) {
        if (featOkTst && casFeat_relations == null)
      jcas.throwFeatMissing("relations", "info.chenli.litway.corpora.Dependency");
    return ll_cas.ll_getRefValue(addr, casFeatCode_relations);
  }
  /** @generated */    
  public void setRelations(int addr, int v) {
        if (featOkTst && casFeat_relations == null)
      jcas.throwFeatMissing("relations", "info.chenli.litway.corpora.Dependency");
    ll_cas.ll_setRefValue(addr, casFeatCode_relations, v);}
    
  
 
  /** @generated */
  final Feature casFeat_modifiers;
  /** @generated */
  final int     casFeatCode_modifiers;
  /** @generated */ 
  public int getModifiers(int addr) {
        if (featOkTst && casFeat_modifiers == null)
      jcas.throwFeatMissing("modifiers", "info.chenli.litway.corpora.Dependency");
    return ll_cas.ll_getRefValue(addr, casFeatCode_modifiers);
  }
  /** @generated */    
  public void setModifiers(int addr, int v) {
        if (featOkTst && casFeat_modifiers == null)
      jcas.throwFeatMissing("modifiers", "info.chenli.litway.corpora.Dependency");
    ll_cas.ll_setRefValue(addr, casFeatCode_modifiers, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Dependency_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_sentenceId = jcas.getRequiredFeatureDE(casType, "sentenceId", "uima.cas.String", featOkTst);
    casFeatCode_sentenceId  = (null == casFeat_sentenceId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sentenceId).getCode();

 
    casFeat_heads = jcas.getRequiredFeatureDE(casType, "heads", "uima.cas.IntegerList", featOkTst);
    casFeatCode_heads  = (null == casFeat_heads) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_heads).getCode();

 
    casFeat_relations = jcas.getRequiredFeatureDE(casType, "relations", "uima.cas.StringList", featOkTst);
    casFeatCode_relations  = (null == casFeat_relations) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relations).getCode();

 
    casFeat_modifiers = jcas.getRequiredFeatureDE(casType, "modifiers", "uima.cas.IntegerList", featOkTst);
    casFeatCode_modifiers  = (null == casFeat_modifiers) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_modifiers).getCode();

  }
}



    