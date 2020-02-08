package models

data class TreeNode(var children : MutableMap<String, TreeNode> = HashMap(), var label: String? = null) {
    fun addChild(childIn : String, childNode: TreeNode) = children.put(childIn, childNode)

}