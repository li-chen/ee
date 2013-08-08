

/* First created by JCasGen Wed Mar 13 14:49:30 GMT 2013 */
package info.chenli.litway.corpora;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Aug 08 16:36:25 BST 2013
 * XML source: /automount/isilon4_ifs-research/textmining/chenli/projects/bionlp/2013/eventExtractor/desc/typeSystemDescriptor.xml
 * @generated */
public class Sentence extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Sentence.class);
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
  protected Sentence() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Sentence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Sentence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Sentence(JCas jcas, int begin, int end) {
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
  public int getId() {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Sentence");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Sentence_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(int v) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "info.chenli.litway.corpora.Sentence");
    jcasType.ll_cas.ll_setIntValue(addr, ((Sentence_Type)jcasType).casFeatCode_id, v);}    
   
    
  //*--------------*
  //* Feature: dependencies

  /** getter for dependencies - gets 
   * @generated */
  public Dependency getDependencies() {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_dependencies == null)
      jcasType.jcas.throwFeatMissing("dependencies", "info.chenli.litway.corpora.Sentence");
    return (Dependency)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_dependencies)));}
    
  /** setter for dependencies - sets  
   * @generated */
  public void setDependencies(Dependency v) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_dependencies == null)
      jcasType.jcas.throwFeatMissing("dependencies", "info.chenli.litway.corpora.Sentence");
    jcasType.ll_cas.ll_setRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_dependencies, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    