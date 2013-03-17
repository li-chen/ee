
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
 * Updated by JCasGen Thu Mar 14 08:11:00 GMT 2013
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
  final Feature casFeat_EventType;
  /** @generated */
  final int     casFeatCode_EventType;
  /** @generated */ 
  public String getEventType(int addr) {
        if (featOkTst && casFeat_EventType == null)
      jcas.throwFeatMissing("EventType", "info.chenli.ee.corpora.Trigger");
    return ll_cas.ll_getStringValue(addr, casFeatCode_EventType);
  }
  /** @generated */    
  public void setEventType(int addr, String v) {
        if (featOkTst && casFeat_EventType == null)
      jcas.throwFeatMissing("EventType", "info.chenli.ee.corpora.Trigger");
    ll_cas.ll_setStringValue(addr, casFeatCode_EventType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Trigger_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_EventType = jcas.getRequiredFeatureDE(casType, "EventType", "uima.cas.String", featOkTst);
    casFeatCode_EventType  = (null == casFeat_EventType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_EventType).getCode();

  }
}



    