package uk.gov.homeoffice.drt.training

import org.specs2.mutable.Specification
class TrainingDataSpec extends Specification {
  val trainingData =
    """
      |[{"id":[1],"uploadTime":"1686066599088","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[2],"uploadTime":"1686066891940","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[3],"uploadTime":"1686068871259","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[4],"uploadTime":"1686069026706","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[5],"uploadTime":"1686069182159","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[6],"uploadTime":"1686069368240","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[7],"uploadTime":"1686069689034","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[8],"uploadTime":"1686070223212","fileName":["test2.mov"],"title":["Test2"],"markdownContent":"Here is markdown example test3"},
      |{"id":[9],"uploadTime":"1686070683558","fileName":["test2.mov"],"title":["RTest4"],"markdownContent":"Here is markdown example test4\r\n\r\n- Some information 1\r\n- Some information 2 \r\n- Some information 3"}]
      |""".stripMargin

  val trainingData1 =
    """
      |[{"id":[1],"uploadTime":"2023-06-06 15:49:59.088","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[2],"uploadTime":"2023-06-06 15:54:51.94","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[3],"uploadTime":"2023-06-06 16:27:51.259","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[4],"uploadTime":"2023-06-06 16:30:26.706","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[5],"uploadTime":"2023-06-06 16:33:02.159","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[6],"uploadTime":"2023-06-06 16:36:08.24","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[7],"uploadTime":"2023-06-06 16:41:29.034","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example"},
      |{"id":[8],"uploadTime":"2023-06-06 16:50:23.212","fileName":["test2.mov"],"title":["Test2"],"markdownContent":"Here is markdown example test3"},
      |{"id":[9],"uploadTime":"2023-06-06 16:58:03.558","fileName":["test2.mov"],"title":["RTest4"],"markdownContent":"Here is markdown example test4\r\n\r\n- Some information 1\r\n- Some information 2 \r\n- Some information 3"}]
      |""".stripMargin
  "" >> {
    "Then I should be able to serialise and deserialise it" >> {
     val a = TrainingData.getTrainingDataConversion(trainingData)
      a.map(println(_))
      a must beAnInstanceOf[Seq[TrainingData]]
    }
  }
}
