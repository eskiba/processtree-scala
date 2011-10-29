package edu.luc.etl.osdi.processtree.scala

import scala.collection.mutable.{ArrayBuffer, Buffer, HashMap, MultiMap, Set}
import scala.math.{max, min}
import scala.collection.JavaConversions.enumerationAsScalaIterator
import java.io.{BufferedReader, InputStreamReader, BufferedWriter, OutputStreamWriter}

object Main {

  val IO_BUF_SIZE = 8192
  val CHILD_LIST_SIZE = 16

  def parseLine(header: String) = {
    val cols = new java.util.StringTokenizer(header).toList
    val iPid = cols indexOf "PID"
    val iPpid = cols indexOf "PPID"
    val iCmd = max(header indexOf "CMD", header indexOf "COMMAND")
    require (iPid >= 0, "required header field PID missing!")
    require (iPpid >= 0, "required header field PPID missing!")
    require (iCmd > max(iPid, iPpid), "required header field CMD or COMMAND missing or not last!")
    (line: String) => {
      val sTok = new java.util.StringTokenizer(line)
      val words = (0 to max(iPid, iPpid)).map(_ => sTok.nextToken())
      (words(iPid).toInt, words(iPpid).toInt, line.substring(iCmd))
    }
  }

  def main(args: Array[String]) = {
    val out = new BufferedWriter(new OutputStreamWriter(System.out), IO_BUF_SIZE);
    val in = new BufferedReader(new InputStreamReader(System.in), IO_BUF_SIZE)
    val parse = parseLine(in.readLine())

    val pmap = new HashMap[Int, String]
    val tmap = new HashMap[Int, Buffer[Int]] // with MultiMap[Int, Int]

    val start = System.currentTimeMillis

    var line = in.readLine()
    while (line != null) {
      val (pid, ppid, cmd) = parse(line)
      pmap += ((pid, cmd))
//      tmap.addBinding(p.ppid, p.pid)
      if (! tmap.contains(ppid))
        tmap += ((ppid, new ArrayBuffer[Int](CHILD_LIST_SIZE)))
      tmap(ppid) += pid
      line = in.readLine()
     }

    def printTree(l: Int, i: Int) {
      for (i <- 0 until l)
    	  out.append(' ')
      out.append(i.toString)
	  out.append(": ")
	  out.append(pmap(i))
	  out.newLine();
      if (tmap.contains(i))
        tmap(i).foreach(printTree(l + 1, _))
    }

    tmap(0).foreach(printTree(0, _))
    out.flush()

    println(System.currentTimeMillis - start + " ms");
  }
}
