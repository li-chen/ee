
/* First created by JCasGen Tue Mar 05 17:56:53 GMT+08:00 2013 */
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
public class Trigger_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Trigger_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Trigger_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Trigger(addr, Trigger_Type.this);
  			   Trigger_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Trigger(addr, Trigger_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Trigger.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("info.chenli.ee.corpora.Trigger");



  /** @generated */ 
  public String getEventType(int addr) {
        if (featOkTst && casFeat_eventType == null)
      jcas.throwFeatMissing("eventType", "info.chenli.ee.corpora.Trigger");
    return ll_cas.ll_getStringValue(addr, casFeatCode_eventType);
  }
  /** @generated */    
  public void setEventType(int addr, String v) {
        if (featOkTst && casFeat_eventType == null)
      jcas.throwFeatMissing("eventType", "info.chenli.ee.corpora.Trigger");
    ll_cas.ll_setStringValue(addr, casFeatCode_eventType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_eventType2;
  /** @generated */
  final int     casFeatCode_eventType2;
  /** @generated */ 
  public String getEventType2(int addr) {
        if (featOkTst && casFeat_eventType2 == null)
      jcas.throwFeatMissing("eventType2", "info.chenli.ee.corpora.Trigger");
    return ll_cas.ll_getStringValue(addr, casFeatCode_eventType2);
  }
  /** @generated */    
  public void setEventType2(int addr, String v) {
        if (featOkTst && casFeat_eventType2 == null)
      jcas.throwFeatMissing("eventType2", "info.chenli.ee.corpora.Trigger");
    ll_cas.ll_setStringValue(addr, casFeatCode_eventType2, v);}
    
  



  /** @generated */
  final Feature casFeat_id;
  /** @generated */
  final int     casFeatCode_id;
  /** @generated */ 
  public String getId(int addr) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Trigger");
    return ll_cas.ll_getStringValue(addr, casFeatCode_id);
  }
  /** @generated */    
  public void setId(int addr, String v) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "info.chenli.ee.corpora.Trigger");
    ll_cas.ll_setStringValue(addr, casFeatCode_id, v);}
    
  
 
  /** @generated */
  final Feature casFeat_eventType;
  /** @generated */
  final int     casFeatCode_eventType;
  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Trigger_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_id = jcas.getRequiredFeatureDE(casType, "id", "uima.cas.String", featOkTst);
    casFeatCode_id  = (null == casFeat_id) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_id).getCode();

 
    casFeat_eventType = jcas.getRequiredFeatureDE(casType, "eventType", "uima.cas.String", featOkTst);
    casFeatCode_eventType  = (null == casFeat_eventType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_eventType).getCode();

 
    casFeat_eventType2 = jcas.getRequiredFeatureDE(casType, "eventType2", "uima.cas.String", featOkTst);
    casFeatCode_eventType2  = (null == casFeat_eventType2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_eventType2).getCode();

  }
}



    