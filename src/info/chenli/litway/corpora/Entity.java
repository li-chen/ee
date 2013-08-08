

/* First created by JCasGen Thu Aug 08 16:36:25 BST 2013 */
package info.chenli.litway.corpora;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Aug 08 16:36:25 BST 2013
 * XML source: /automount/isilon4_ifs-research/textmining/chenli/projects/bionlp/2013/eventExtractor/desc/typeSystemDescriptor.xml
 * @generated */
public class Entity extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Entity.class);
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
  protected Entity() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Entity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Entity(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Entity(JCas jcas, int begin, int end) {
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
  //* Feature: id

  /** getter for id - gets 
   * @generated */
  public String getId() {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Entity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Entity_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(String v) {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Entity");
    jcasType.ll_cas.ll_setStringValue(addr, ((Entity_Type)jcasType).casFeatCode_id, v);}    
   
    
  //*--------------*
  //* Feature: entityType

  /** getter for entityType - gets 
   * @generated */
  public String getEntityType() {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_entityType == null)
      jcasType.jcas.throwFeatMissing("entityType", "info.chenli.litway.corpora.Entity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Entity_Type)jcasType).casFeatCode_entityType);}
    
  /** setter for entityType - sets  
   * @generated */
  public void setEntityType(String v) {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_entityType == null)
      jcasType.jcas.throwFeatMissing("entityType", "info.chenli.litway.corpora.Entity");
    jcasType.ll_cas.ll_setStringValue(addr, ((Entity_Type)jcasType).casFeatCode_entityType, v);}    
  }

    