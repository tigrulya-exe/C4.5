import models.DataEntity
import models.TreeNode

class Classifier(private val head : TreeNode) {
    fun classify(entity: DataEntity) : String? = setLabel(entity, head)

    private fun setLabel(entity: DataEntity, treeNode: TreeNode) : String?{
        treeNode.label?.let {
            return it
        }

        val attribute = entity.attributes[treeNode.attributeIndex]
        val nextNode = treeNode.children[attribute] ?: let {
            return null
        }

        return setLabel(entity, nextNode)
    }
}

fun main() {
    val entities = parseCsv("test.csv")
    val trainer = TreeTrainer(entities)
    val head = TreeNode()
    trainer.train(head, entities)

    val classifier = Classifier(head)
    parseCsv("toClassify.csv").forEach{
        println(classifier.classify(it))
    }
}