import models.DataEntity
import java.io.File

fun parseCsv(fileName: String): List<DataEntity> {
    val dataEntities = mutableListOf<DataEntity>();
    File(fileName).bufferedReader().forEachLine {
        val tokens = it.split("[\\s,]+".toRegex())
        dataEntities.add(DataEntity(tokens))
    }

    return dataEntities;
}