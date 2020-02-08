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

    private data class AttributeContext(val attributeInfo : Double, val splitInfos : Double)

    private fun initAttributes(trainingEntities: List<DataEntity>) {
        trainingEntities.forEach {
            for (i in it.attributes.indices) {
                attributesMap[i] = attributesMap[i] ?: HashSet()
                attributesMap[i]?.add(it.attributes[i])
            }
        }
    }

    private fun getSplitInfo(trainingEntities: List<DataEntity>, attributeIndex: Int) : Double{
        val frequency = getFrequency(trainingEntities, attributeIndex)
        return calculateEntropy(trainingEntities, frequency)
    }

    private fun getInfo(trainingEntities: List<DataEntity>) : Double{
        val frequency = getFrequency(trainingEntities, trainingEntities.size - 1)
        return -1 * calculateEntropy(trainingEntities, frequency)
    }

    private fun getAttributeInfo(trainingEntities: List<DataEntity>, attributeIndex: Int) : Double{
        val entitiesCount = trainingEntities.size
        val classFrequency = getFrequency(trainingEntities, entitiesCount - 1)
        return classFrequency.values.fold(0.0){ acc, freq ->
            acc + (freq/entitiesCount) * getInfo(trainingEntities)
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

    private fun calculateEntropy(trainingEntities: List<DataEntity>, frequency : Map<String, Int>) : Double{
        val entitiesCount = trainingEntities.size
        return frequency.values.fold(0.0){ acc, freq ->
            acc + (freq/entitiesCount) * log((freq/entitiesCount).toDouble(), 2.0)
        }
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

    private fun makeLeaf(treeNode: TreeNode, attributeValue : String, trainingEntities: List<DataEntity>){
        val frequency = getFrequency(trainingEntities, trainingEntities.size - 1)
        val leaf = TreeNode()
        leaf.label = frequency.maxBy { it.value }?.key
        treeNode.addChild(attributeValue, leaf)
    }

    fun train(treeNode: TreeNode?, trainingEntities: List<DataEntity>) : Boolean {
        treeNode ?: return false

        if(trainingEntities.isEmpty()){
            return false
        }

        val info = getInfo(trainingEntities)
        val attributeContexts = ArrayList<AttributeContext>()

        for(i in attributesMap.keys){
            val attrInfo = getAttributeInfo(trainingEntities, i)
            val splitInfo =  getSplitInfo(trainingEntities, i)
            attributeContexts.add(AttributeContext(attrInfo, splitInfo))
        }

        var max = 0.0
        val indexToDivide= attributeContexts.foldIndexed(0){ index, acc, context ->
            val gainRatio = (info - context.attributeInfo) / context.splitInfos
            if(gainRatio > max) {
                max = gainRatio
                index
            } else acc
        }

        divideByAttribute(trainingEntities, indexToDivide).forEach{
            val child = TreeNode()
            treeNode.addChild(it.key, child)
            if(!train(child, it.value))
                makeLeaf(treeNode, it.key, trainingEntities)
        }

        return true
    }
}


fun main(){
    val entities = parseCsv("test.csv")
    val trainer = TreeTrainer(entities)

    trainer.train(TreeNode(), entities)
}
