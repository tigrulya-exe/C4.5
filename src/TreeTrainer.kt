import models.DataEntity
import models.TreeNode
import kotlin.math.log

class TreeTrainer() {
    // key - attribute position in entity attribute list
    // value - possible attribute values
    private val attributesMap = HashMap<Int, MutableSet<String>>()

    constructor(trainingEntities: List<DataEntity>) : this(){
        initAttributes(trainingEntities)
    }

    private fun initAttributes(trainingEntities: List<DataEntity>) {
        trainingEntities.forEach {
            for (i in it.attributes.indices) {
                attributesMap[i] = attributesMap[i] ?: HashSet()
                attributesMap[i]?.add(it.attributes[i])
            }
        }
    }

    private fun getSplitInfo(groups : Map<String, MutableList<DataEntity>>, entitiesCount : Int) : Double{
        return groups.values.fold(0.0){ acc, entities ->
            val probability = entities.size.toDouble() / entitiesCount
            if(probability == 0.0) acc else acc + probability * log(probability, 2.0)
        }
    }

    private fun getInfo(trainingEntities: List<DataEntity>) : Double{
        val frequency = getFrequency(trainingEntities, attributesMap.size - 1)

        return -1 * frequency.values.fold(0.0){ acc, freq ->
            val probability = freq.toDouble()/trainingEntities.size
            acc + probability * log(probability, 2.0)
        }
    }

    private fun getAttributeInfo(groups : Map<String, MutableList<DataEntity>>, entitiesCount : Int) : Double{
        return groups.values.fold(0.0){ acc, entities ->
            acc + (entities.size.toDouble() / entitiesCount) * getInfo(entities)
        }
    }

    private fun getFrequency(trainingEntities: List<DataEntity>, attributeIndex: Int): Map<String, Int> {
        val frequency : MutableMap<String, Int> = HashMap()

        trainingEntities.forEach{ entity ->
            val attributeValue = entity.attributes[attributeIndex]
            frequency[attributeValue] = frequency.getOrDefault(attributeValue, 0) + 1
        }

        return frequency
    }

    private fun divideByAttribute(trainingEntities: List<DataEntity>, attributeIndex: Int) : Map<String, MutableList<DataEntity>>{
        val groups : MutableMap<String, MutableList<DataEntity>> = HashMap()
        attributesMap[attributeIndex]?.forEach{
            groups[it] = ArrayList()
        }

        trainingEntities.forEach{
            val attributeValue = it.attributes[attributeIndex]
            groups[attributeValue]?.add(it)
        }

        return groups
    }

    private fun makeLeaf(treeNode: TreeNode, attributeValue : String, frequency: Map<String, Int>){
        val leaf = TreeNode()
        leaf.label = frequency.maxBy { it.value }?.key
        treeNode.addChild(attributeValue, leaf)
    }

    fun train(treeNode: TreeNode?, trainingEntities: List<DataEntity>) : Boolean {
        if(treeNode == null || trainingEntities.isEmpty()){
            return false
        }

        val classFrequency = getFrequency(trainingEntities, attributesMap.size - 1)
        if(classFrequency.size == 1){
            treeNode.label = trainingEntities[0].attributes[attributesMap.size - 1];
            return true;
        }

        val info = getInfo(trainingEntities)
        var maxGainRatio =  - Double.MAX_VALUE
        var groupsDividedByAttribute : Map<String, MutableList<DataEntity>>? = null

        for(i in 0 until attributesMap.size - 1){
            val groups = divideByAttribute(trainingEntities, i)
            val attrInfo = getAttributeInfo(groups, trainingEntities.size)
            val splitInfo =  getSplitInfo(groups, trainingEntities.size)
            val gainRatio = (info - attrInfo) / splitInfo

            if(gainRatio > maxGainRatio){
                maxGainRatio = gainRatio
                treeNode.attributeIndex = i
                groupsDividedByAttribute = groups
            }

        }

        groupsDividedByAttribute?.forEach{
            val child = TreeNode()
            treeNode.addChild(it.key, child)
            if(!train(child, it.value))
                makeLeaf(treeNode, it.key, classFrequency)
        }

        return true
    }
}


fun main(){
    val entities = parseCsv("test.csv")
    val trainer = TreeTrainer(entities)
    val head = TreeNode()
    trainer.train(head, entities)

    println(head)
}
