package trymorganey

import java.io._

import me.rexim.morganey.ast.MorganeyBinding
import me.rexim.morganey.interpreter.ReplContext
import me.rexim.morganey.module.ModuleFinder

import scala.util.Try

case class Session(
  sid:     Long,
  content: Array[Byte],
  created: Long
) {

  def context(finder: ModuleFinder): Option[ReplContext] = Try {
    val stream   = new ByteArrayInputStream(content)

    /* Workaround for problem with deserialization of case classes
     * see: http://stackoverflow.com/a/22375260
     */
    val objectIn = new ObjectInputStream(stream) {
      override def resolveClass(desc: java.io.ObjectStreamClass): Class[_] = {
        try { Class.forName(desc.getName, false, getClass.getClassLoader) }
        catch { case ex: ClassNotFoundException => super.resolveClass(desc) }
      }
    }

    val obj      = objectIn.readObject()
    objectIn.close()
    val bindings = obj.asInstanceOf[List[MorganeyBinding]]
    ReplContext(bindings, finder)
  }.toOption

  def update(context: ReplContext): Session = {
    val stream    = new ByteArrayOutputStream()
    val objectOut = new ObjectOutputStream(stream)
    objectOut.writeObject(context.bindings)
    objectOut.flush()
    objectOut.close()
    Session(sid, stream.toByteArray, System.currentTimeMillis())
  }

  override def toString: String =
    s"Session($sid, ${content.length} bytes, $created)"

}
