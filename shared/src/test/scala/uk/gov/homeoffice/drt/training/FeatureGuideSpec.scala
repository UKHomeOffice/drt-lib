package uk.gov.homeoffice.drt.training

import org.specs2.mutable.Specification

class FeatureGuideSpec extends Specification {
  val featureGuide =
    """
      |[{"id":[1],"uploadTime":"1686066599088","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example","published": "true"},
      |{"id":[2],"uploadTime":"1686066891940","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example","published": "true"},
      |{"id":[3],"uploadTime":"1686068871259","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example","published": "true"},
      |{"id":[4],"uploadTime":"1686069026706","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example","published": "true"},
      |{"id":[5],"uploadTime":"1686069182159","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example","published": "true"},
      |{"id":[6],"uploadTime":"1686069368240","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example","published": "true"},
      |{"id":[7],"uploadTime":"1686069689034","fileName":["test1"],"title":["Test1"],"markdownContent":"Here is markdown example","published": "true"},
      |{"id":[8],"uploadTime":"1686070223212","fileName":["test2.mov"],"title":["Test2"],"markdownContent":"Here is markdown example test3","published": "true"},
      |{"id":[9],"uploadTime":"1686070683558","fileName":["test2.mov"],"title":["RTest4"],"markdownContent":"Here is markdown example test4\r\n\r\n- Some information 1\r\n- Some information 2 \r\n- Some information 3","published": "true"}]
      |""".stripMargin


  "Given sequence of FeatureGuide json string" >> {
    "Then I should be able to serialise and de-serialise it" >> {
      val a = FeatureGuide.getFeatureGuideConversion(featureGuide)
      a must beAnInstanceOf[Seq[FeatureGuide]]
    }
  }
}
