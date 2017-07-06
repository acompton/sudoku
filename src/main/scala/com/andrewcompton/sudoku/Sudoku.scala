package com.andrewcompton.sudoku

import scala.collection.Set
import scala.collection.parallel.ParSeq
import scala.util.Try

object Sudoku {
  case class GridFormat(dim: Int) {
    require(dim % 3 == 0, s"invalid grid dimension ($dim)")

    val groupDim: Int = dim / 3

    val elems: Set[Int] = (1 to dim).toSet

    def parse(s: String): Grid = {
      val values = s.filterNot(Character.isWhitespace).map({
        case i if Character.isDigit(i) ⇒ Solved(i - '0')
        case _ ⇒ Unsolved
      }).toVector

      require(values.length == dim * dim, s"wrong dimensions (${values.length} != ${dim*dim})")

      new Grid(values, this)
    }
  }

  sealed abstract class Square(val isSolved: Boolean)
  case class Solved(i: Int) extends Square(true)
  case object Unsolved extends Square(false)

  type Group = Seq[Square]

  class Grid(val state: Vector[Square], val fmt: GridFormat) {
    import fmt._

    private val unsolved = {
      val candidatesByIndex = for (i <- state.indices if !state(i).isSolved) yield {
        val (c, r) = (i % dim, i / dim)
        val rowRemaining = remaining(row(r))
        val colRemaining = remaining(col(c))
        val boxRemaining = remaining(boxAt(c, r))
        val candidates = rowRemaining.intersect(colRemaining).intersect(boxRemaining)
        (i, candidates)
      }

      candidatesByIndex.sortBy(_._2.size)
    }

    def reduce(): ParSeq[Grid] =
      if (!isValid) ParSeq.empty
      else {
        val (solved, remaining) = unsolved.partition(_._2.size == 1)

        val newState = solved.foldLeft(state) {
          case (s, (i, n)) => s.updated(i, Solved(n.head))
        }

        remaining.headOption match {
          case Some((i, candidates)) =>
            candidates.toList.par.flatMap(n =>
              new Grid(newState.updated(i, Solved(n)), fmt).reduce()
            )
          case None => ParSeq(this)
        }
      }

    private def remaining(g: Group) = {
      val solved = g.collect {
        case Solved(i) => i
      }.toSet

      elems.diff(solved)
    }

    def isValid: Boolean =
      rows.find(containsDuplicates)
        .orElse(cols.find(containsDuplicates))
        .orElse(boxes.find(containsDuplicates))
        .isEmpty

    private def containsDuplicates(group: Group): Boolean = {
      val solved = group.filter(_.isSolved)
      solved.size != solved.toSet.size
    }

    def apply(c: Int, r: Int): Square = state(c + r * dim)

    def rows: Seq[Group] = for(r ← 0 until dim) yield row(r)
    def cols: Seq[Group] = for(c ← 0 until dim) yield col(c)
    def boxes: Seq[Group] = for(g ← 0 until dim) yield box(g)

    def row(r: Int): Group = for (c ← 0 until dim) yield apply(c, r)
    def col(c: Int): Group = for (r ← 0 until dim) yield apply(c, r)

    def box(g: Int): Group = {
      val (colStart, rowStart) = (g % groupDim * groupDim, g / groupDim * groupDim)
      for(gr ← 0 until groupDim; gc ← 0 until groupDim)
        yield apply(gc + colStart, gr + rowStart)
    }

    def boxAt(c: Int, r: Int): Group = box((c / 3) + (r / 3 * 3))

    override def toString: String = {
      val stateDesc = state.zipWithIndex.map {
        case (Solved(i), _) ⇒ i.toString
        case (Unsolved, i) ⇒ "?"
      }

      val fmtWidth = stateDesc.map(_.length).max
      val fmt = s"%${fmtWidth}s"
      val gap = "═" * fmtWidth
      val fill = s"═$gap═$gap═$gap═"
      val rowFmt = s"║ $fmt $fmt $fmt ║ $fmt $fmt $fmt ║ $fmt $fmt $fmt ║${System.lineSeparator()}"
      val boxFmt = s"%c$fill%c$fill%c$fill%c${System.lineSeparator()}"

      val sb = new StringBuilder
      sb.append(boxFmt.format('╔','╦','╦','╗'))
        .append(rowFmt).append(rowFmt).append(rowFmt)
        .append(boxFmt.format('╠','╬','╬','╣'))
        .append(rowFmt).append(rowFmt).append(rowFmt)
        .append(boxFmt.format('╠','╬','╬','╣'))
        .append(rowFmt).append(rowFmt).append(rowFmt)
        .append(boxFmt.format('╚','╩','╩','╝'))

      sb.toString.format(stateDesc: _*)
    }
  }
  
  def main(args: Array[String]): Unit = {
    val g = args match {
      case Array("-i") => Try(io.Source.fromInputStream(System.in).mkString).toOption
      case Array("-f", file) => Try(io.Source.fromFile(file).mkString).toOption
      case _ =>
        println(
          s"""Provide one of:
             |  -i          Read a grid from standard input
             |  -f <path>   Read a grid from the specified file
           """.stripMargin)
        None
    }

    if (g.isEmpty) {
      println("Error reading input")
      return
    }

    val unsolved = GridFormat(9).parse(g.get)

    val solutions = unsolved.reduce()

    if (solutions.isEmpty) print("No solutions found")
    else if (solutions.size == 1) print("Found a unique solution")
    else print("Multiple solutions found")

    for (solution <- solutions) {
      println()
      println(solution)
    }
  }
}