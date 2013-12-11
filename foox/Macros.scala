package foox

import scala.reflect.macros.WhiteboxContext
import scala.language.experimental.macros
import scala.reflect.api.Liftable
import scala.reflect.macros.Universe
import scala.reflect.api.Position

object Macros {

  implicit def liftableCaseClass[T]: Liftable[T] = macro impl[T]

  def impl[T: c.WeakTypeTag](c: WhiteboxContext) = {
    import c.universe._

    val T = weakTypeOf[T]
    if (!T.typeSymbol.asClass.isCaseClass) c.abort(c.enclosingPosition, "Not a case class")
    else {
      //val params = T.members.collect { case x: MethodSymbol if x.isCaseAccessor => q"Select(Ident(TermName(cc)), TermName(x))" }.toList.reverse
      val name = T.typeSymbol.name.decoded // What is the difference between encoded and decoded ?

      //println("PARAMS : " + showRaw(params))
      
      q"""
        import scala.reflect.api.Liftable
        implicit object Foo extends Liftable[$T] {
          def apply(universe: reflect.api.Universe, cc: $T): universe.Tree = {
            
            val ttree = universe.Ident(universe.TermName($name))
            
            universe.Apply(universe.Select(universe.New(ttree), universe.nme.CONSTRUCTOR), universe.Select(universe.Ident(universe.TermName(cc)), universe.TermName(x)))
            
          }
        }
        Foo
     """

    }

  }

}