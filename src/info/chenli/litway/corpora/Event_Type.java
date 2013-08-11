
/* First created by JCasGen Tue Mar 05 19:15:26 GMT+08:00 2013 */
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
 * Updated by JCasGen Thu Aug 08 16:36:25 BST 2013
 * @generated */
public class Event_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Event_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Event_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Event(addr, Event_Type.this);
  			   Event_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Event(addr, Event_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Event.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("info.chenli.litway.corpora.Event");



  /** @generated */
  final Feature casFeat_themes;
  /** @generated */
  final int     casFeatCode_themes;
  /** @generated */ 
  public int getThemes(int addr) {
        if (featOkTst && casFeat_themes == null)
      jcas.throwFeatMissing("themes", "info.chenli.litway.corpora.Event");
    return ll_cas.ll_getRefValue(addr, casFeatCode_themes);
  }
  /** @generated */    
  public void setThemes(int addr, int v) {
        if (featOkTst && casFeat_themes == null)
      jcas.throwFeatMissing("themes", "info.chenli.litway.corpora.Event");
    ll_cas.ll_setRefValue(addr, casFeatCode_themes, v);}
    
   /** @generated */
  public String getThemes(int addr, int i) {
        if (featOkTst && casFeat_themes == null)
      jcas.throwFeatMissing("themes", "info.chenli.litway.corpora.Event");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i);
  }
   
  /** @generated */ 
  public void setThemes(int addr, int i, String v) {
        if (featOkTst && casFeat_themes == null)
      jcas.throwFeatMissing("themes", "info.chenli.litway.corpora.Event");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_cause;
  /** @generated */
  final int     casFeatCode_cause;
  /** @generated */ 
  public String getCause(int addr) {
        if (featOkTst && casFeat_cause == null)
      jcas.throwFeatMissing("cause", "info.chenli.litway.corpora.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_cause);
  }
  /** @generated */    
  public void setCause(int addr, String v) {
        if (featOkTst && casFeat_cause == null)
      jcas.throwFeatMissing("cause", "info.chenli.litway.corpora.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_cause, v);}
    
  
 
  /** @generated */
  final Feature casFeat_product;
  /** @generated */
  final int     casFeatCode_product;
  /** @generated */ 
  public String getProduct(int addr) {
        if (featOkTst && casFeat_product == null)
      jcas.throwFeatMissing("product", "info.chenli.litway.corpora.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_product);
  }
  /** @generated */    
  public void setProduct(int addr, String v) {
        if (featOkTst && casFeat_product == null)
      jcas.throwFeatMissing("product", "info.chenli.litway.corpora.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_product, v);}
    
  
 
  /** @generated */
  final Feature casFeat_arguments;
  /** @generated */
  final int     casFeatCode_arguments;
  /** @generated */ 
  public int getArguments(int addr) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "info.chenli.litway.corpora.Event");
    return ll_cas.ll_getRefValue(addr, casFeatCode_arguments);
  }
  /** @generated */    
  public void setArguments(int addr, int v) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "info.chenli.litway.corpora.Event");
    ll_cas.ll_setRefValue(addr, casFeatCode_arguments, v);}
    
   /** @generated */
  public int getArguments(int addr, int i) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "info.chenli.litway.corpora.Event");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i);
  }
   
  /** @generated */ 
  public void setArguments(int addr, int i, int v) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "info.chenli.litway.corpora.Event");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i, v);
  }
 



  /** @generated */
  final Feature casFeat_id;
  /** @generated */
  final int     casFeatCode_id;
  /** @generated */ 
  public String getId(int addr) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_id);
  }
  /** @generated */    
  public void setId(int addr, String v) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_id, v);}
    
  
 
  /** @generated */
  final Feature casFeat_trigger;
  /** @generated */
  final int     casFeatCode_trigger;
  /** @generated */ 
  public int getTrigger(int addr) {
        if (featOkTst && casFeat_trigger == null)
      jcas.throwFeatMissing("trigger", "info.chenli.litway.corpora.Event");
    return ll_cas.ll_getRefValue(addr, casFeatCode_trigger);
  }
  /** @generated */    
  public void setTrigger(int addr, int v) {
        if (featOkTst && casFeat_trigger == null)
      jcas.throwFeatMissing("trigger", "info.chenli.litway.corpora.Event");
    ll_cas.ll_setRefValue(addr, casFeatCode_trigger, v);}
    
  
 
  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Event_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_id = jcas.getRequiredFeatureDE(casType, "id", "uima.cas.String", featOkTst);
    casFeatCode_id  = (null == casFeat_id) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_id).getCode();

 
    casFeat_trigger = jcas.getRequiredFeatureDE(casType, "trigger", "info.chenli.litway.corpora.Trigger", featOkTst);
    casFeatCode_trigger  = (null == casFeat_trigger) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_trigger).getCode();

 
    casFeat_themes = jcas.getRequiredFeatureDE(casType, "themes", "uima.cas.StringArray", featOkTst);
    casFeatCode_themes  = (null == casFeat_themes) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_themes).getCode();

 
    casFeat_cause = jcas.getRequiredFeatureDE(casType, "cause", "uima.cas.String", featOkTst);
    casFeatCode_cause  = (null == casFeat_cause) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_cause).getCode();

 
    casFeat_product = jcas.getRequiredFeatureDE(casType, "product", "uima.cas.String", featOkTst);
    casFeatCode_product  = (null == casFeat_product) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_product).getCode();

 
    casFeat_arguments = jcas.getRequiredFeatureDE(casType, "arguments", "uima.cas.FSArray", featOkTst);
    casFeatCode_arguments  = (null == casFeat_arguments) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_arguments).getCode();

  }
}



    