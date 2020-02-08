import models.DataEntity
import models.TreeNode
import kotlin.math.ln
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

    private fun getSplitInfo(attributeIndex: Int, trainingEntities: List<DataEntity>){
        val entitiesCount = trainingEntities.size
        val frequency : MutableMap<String, Int> = HashMap()

        trainingEntities.forEach{ entity ->
            entity.attributes.forEach{
                frequency[it] = frequency.getOrDefault(it, 0) + 1
            }
        }

        frequency.values.fold(0.0){ acc, freq ->
            acc + (freq/entitiesCount) * ln((freq/entitiesCount).toDouble())
        }
    }


    fun train(head: TreeNode?, trainingEntities: List<DataEntity>) {
        for(i in attributesMap.keys){

        }
    }
}


fun main(){
    val entities = parseCsv("test.csv")
    val trainer = TreeTrainer(entities)

    trainer.train(null, entities)
}
