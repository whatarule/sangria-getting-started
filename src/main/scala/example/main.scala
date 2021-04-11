package example

import cats.implicits._
import scala.util.chaining._

import sangria.schema._
import sangria.execution._
import sangria.macros._
import sangria.marshalling.circe._
import scala.concurrent.ExecutionContext.Implicits.global

object main extends App {
  def hello = {
    val QueryType = ObjectType(
      "Query",
      fields[Unit, Unit](
        Field("hello", StringType, resolve = _ => "Hello world!")
      )
    )
    val schema = Schema(QueryType)
    val query  = graphql"{ hello }"
    val result = Executor.execute(schema, query)
    result.foreach(res => println(res.spaces2))
  }

  def products = {
    val query = graphql"""
  query MyProduct {
    product(id: "2") { name description picture(size: 500) {width, height, url} }
    products { name }
  }
  """
    import scala.concurrent.Future
    import io.circe.Json
    import SchemaDefinition._
    val result: Future[Json] = Executor.execute(schema, query, new ProductRepo)
    result map println
  }
}

object SchemaDefinition {
  import sangria.schema._
  case class Picture(width: Int, height: Int, url: Option[String])
//  val PictureType = ObjectType(
//    "Picture",
//    "The product picture",
//    fields[Unit, Picture](
//      Field("width", IntType, resolve = _.value.width),
//      Field("height", IntType, resolve = _.value.height),
//      Field("url", OptionType(StringType), description = Some("Picture CDN URL"), resolve = _.value.url)
//    )
//  )

  import sangria.macros.derive._
  implicit val PictureType = deriveObjectType[Unit, Picture](
    ObjectTypeDescription("The product picture"),
    DocumentField("url", "Picture CDN URL")
  )

  trait Identifiable { def id: String }
  val IdentifiableType = InterfaceType(
    "Indetifiable",
    "Entity that can be indentified",
    fields[Unit, Identifiable](Field("id", StringType, resolve = _.value.id))
  )

  case class Product(id: String, name: String, description: String) extends Identifiable {
    def picture(size: Int): Picture =
      Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"))
  }
  val ProductType =
    deriveObjectType[Unit, Product](Interfaces(IdentifiableType), IncludeMethods("picture"))
  class ProductRepo {
    private val Products = List(
      Product("1", "Cheesecake", "Tasty"),
      Product("2", "Health Potion", "+50 HP")
    )
    def product(id: String): Option[Product] = Products find (_.id == id)
    def products: List[Product]              = Products
  }

  val Id = Argument("id", StringType)
  val QueryType = ObjectType(
    "Query",
    fields[ProductRepo, Unit](
      Field(
        "product",
        OptionType(ProductType),
        description = Some("Returns a product with specific `id`.`"),
        arguments = Id :: Nil,
        resolve = c => c.ctx.product(c arg Id)
      ),
      Field(
        "products",
        ListType(ProductType),
        description = Some("Returns a list of all available products."),
        resolve = _.ctx.products
      )
    )
  )
  val schema = Schema(QueryType)
}
