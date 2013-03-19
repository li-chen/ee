
/* First created by JCasGen Tue Mar 05 19:15:26 GMT+08:00 2013 */
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
 * Updated by JCasGen Tue Mar 19 18:58:21 GMT 2013
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
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("info.chenli.ee.corpora.Event");



  /** @generated */
  final Feature casFeat_themes;
  /** @generated */
  final int     casFeatCode_themes;
  /** @generated */ 
  public int getThemes(int addr) {
        if (featOkTst && casFeat_themes == null)
      jcas.throwFeatMissing("themes", "info.chenli.ee.corpora.Event");
    return ll_cas.ll_getRefValue(addr, casFeatCode_themes);
  }
  /** @generated */    
  public void setThemes(int addr, int v) {
        if (featOkTst && casFeat_themes == null)
      jcas.throwFeatMissing("themes", "info.chenli.ee.corpora.Event");
    ll_cas.ll_setRefValue(addr, casFeatCode_themes, v);}
    
   /** @generated */
  public String getThemes(int addr, int i) {
        if (featOkTst && casFeat_themes == null)
      jcas.throwFeatMissing("themes", "info.chenli.ee.corpora.Event");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i);
  }
   
  /** @generated */ 
  public void setThemes(int addr, int i, String v) {
        if (featOkTst && casFeat_themes == null)
      jcas.throwFeatMissing("themes", "info.chenli.ee.corpora.Event");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_themes), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_causes;
  /** @generated */
  final int     casFeatCode_causes;
  /** @generated */ 
  public int getCauses(int addr) {
        if (featOkTst && casFeat_causes == null)
      jcas.throwFeatMissing("causes", "info.chenli.ee.corpora.Event");
    return ll_cas.ll_getRefValue(addr, casFeatCode_causes);
  }
  /** @generated */    
  public void setCauses(int addr, int v) {
        if (featOkTst && casFeat_causes == null)
      jcas.throwFeatMissing("causes", "info.chenli.ee.corpora.Event");
    ll_cas.ll_setRefValue(addr, casFeatCode_causes, v);}
    
   /** @generated */
  public String getCauses(int addr, int i) {
        if (featOkTst && casFeat_causes == null)
      jcas.throwFeatMissing("causes", "info.chenli.ee.corpora.Event");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_causes), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_causes), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_causes), i);
  }
   
  /** @generated */ 
  public void setCauses(int addr, int i, String v) {
        if (featOkTst && casFeat_causes == null)
      jcas.throwFeatMissing("causes", "info.chenli.ee.corpora.Event");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_causes), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_causes), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_causes), i, v);
  }
 



  /** @generated */
  final Feature casFeat_id;
  /** @generated */
  final int     casFeatCode_id;
  /** @generated */ 
  public String getId(int addr) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Event");
    return ll_cas.ll_getStringValue(addr, casFeatCode_id);
  }
  /** @generated */    
  public void setId(int addr, String v) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Event");
    ll_cas.ll_setStringValue(addr, casFeatCode_id, v);}
    
  
 
  /** @generated */
  final Feature casFeat_trigger;
  /** @generated */
  final int     casFeatCode_trigger;
  /** @generated */ 
  public int getTrigger(int addr) {
        if (featOkTst && casFeat_trigger == null)
      jcas.throwFeatMissing("trigger", "info.chenli.ee.corpora.Event");
    return ll_cas.ll_getRefValue(addr, casFeatCode_trigger);
  }
  /** @generated */    
  public void setTrigger(int addr, int v) {
        if (featOkTst && casFeat_trigger == null)
      jcas.throwFeatMissing("trigger", "info.chenli.ee.corpora.Event");
    ll_cas.ll_setRefValue(addr, casFeatCode_trigger, v);}
    
  
 
  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Event_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_id = jcas.getRequiredFeatureDE(casType, "id", "uima.cas.String", featOkTst);
    casFeatCode_id  = (null == casFeat_id) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_id).getCode();

 
    casFeat_trigger = jcas.getRequiredFeatureDE(casType, "trigger", "info.chenli.ee.corpora.Trigger", featOkTst);
    casFeatCode_trigger  = (null == casFeat_trigger) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_trigger).getCode();

 
    casFeat_themes = jcas.getRequiredFeatureDE(casType, "themes", "uima.cas.StringArray", featOkTst);
    casFeatCode_themes  = (null == casFeat_themes) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_themes).getCode();

 
    casFeat_causes = jcas.getRequiredFeatureDE(casType, "causes", "uima.cas.StringArray", featOkTst);
    casFeatCode_causes  = (null == casFeat_causes) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_causes).getCode();

  }
}



    