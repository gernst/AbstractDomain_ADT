package Abstraction



/**
 * AList is an abstract domain of numerical lists (belonging to algebraic data types)
 * It is described by an interval and has three types:
 * -ANil: describes the empty AList
 * -ACons(h,t): has a specified head with associated interval and a tail of type AList
 * -AMany(e): contains both types ANil and ACons
 */
case class ALists(intervals: Intervals){
  import intervals.Interval //import inner Class
  type AInt = Interval  //alias


  sealed trait AList  //"behaviour"
  case object ANil extends AList
  case class ACons(head: AInt, tail: AList) extends AList
  case class AMany(elem: AInt) extends AList


  sealed trait AOption[+A]
  case object ANone extends AOption[Nothing]
  case class ASome[A](get: A) extends AOption[A]
  case class AMaybe[A](get: A) extends AOption[A]

/*Method returns the head of aList object, which is of type AOption[AInt]
*/
  def aHead (l: AList): AOption[AInt] = l match {
    case ANil => ANone
    case ACons(h, _) => ASome(h)
    case AMany(e) => AMaybe(e) //AMany = ANil ≀ ACons(e, Many(e))
  }

  /*Method returns the tail of aList object, which is of type AOption[AInt]
*/
  def aTail(l: AList): AOption[AList] = l match {
    case ANil => ANone
    case ACons(_,t) => ASome(t)
    case AMany(e) => AMaybe(AMany(e))
  }

  /*Method returns the length of aList object, which is of type AOption[AInt]
*/
  def aLength(l: AList): AOption[AInt] = l match {
    case ANil => ANone
    case ACons(_, _) => ASome(Interval(IntegerVal(1), IntegerInf))
    case AMany(_) => ASome(Interval(IntegerVal(0), IntegerInf))
  }

  /*Method checks whether an integer value is a concrete Element of an Interval.
   *It takes two parameters, an integer and an interval of type AInt and returns a boolean
   */
  def isConcreteElementOf_Int(i: Int, ai: AInt): Boolean ={
    intervals.contains(ai, i)
  }


  /*Method checks whether a list is a concrete Element of AList.
   *It takes two parameters, a list with integer values and a abstract AList and returns a boolean
   */
  def isConcreteElementOf_List(l: List[Int], al:AList): Boolean = (l, al) match{
    case (Nil, ANil) => true
    case (Nil, ACons(_,_)) => false
    case (Nil, AMany(_)) => true //AMany = ANil ≀ ACons(e, Many(e))
    case (x::xs, ANil) => false
    case (x::xs, ACons(ax, axs)) => isConcreteElementOf_Int(x, ax) && isConcreteElementOf_List(xs, axs)
    case (x::xs, AMany(ax)) =>
      isConcreteElementOf_Int(x, ax) && isConcreteElementOf_List(xs, al)
  }


  /*Method checks whether an Option[Int] is a concrete Element of an interval AOption[AInt].
   *It takes two parameters, an integer and an interval of type AInt and returns a boolean

   */
  def isConcreteElementOf_Option(o: Option[Int], ao: AOption[AInt]): Boolean = (o,ao) match {
    case (None, ANone) => true
    case (None, ASome(_)) => false
    case (None, AMaybe(_)) => true
    case (Some(_), ANone) => false
    case (Some(h1), ASome(h2)) => intervals.contains(h2, h1)
    case (Some(h1), AMaybe(h2)) => intervals.contains(h2, h1)
  }



  def union_AList(al1: AList, al2: AList) : AList = (al1, al2) match {
    case (ANil, ANil) => ANil
    case (ANil, AMany(e)) => AMany(e)
    case (AMany(e), ANil) => AMany(e)
    case (ANil, ACons(a,as)) => union_AList(AMany(a), as)
    case (ACons(a,as), ANil) => union_AList(AMany(a), as)
    case (AMany(a), AMany(b)) => AMany(intervals.union_Interval(a,b))
    case (ACons(a,as), AMany(e)) => union_AList(AMany(intervals.union_Interval(a,e)), as)
    case (AMany(e), ACons(a,as)) => union_AList(AMany(intervals.union_Interval(a,e)), as)
    case (ACons(a,as), ACons(b, bs)) => ACons(intervals.union_Interval(a,b), union_AList(as,bs))
  }

  //widen -> not symmetric
  def widen_AList(al1: AList, al2: AList) : AList = (al1, al2) match {
    case (ANil, ANil) => ANil
    case (ANil, AMany(e)) => AMany(e)
    case (AMany(e), ANil) => AMany(e)
    case (ANil, ACons(a, as)) => widen_AList(AMany(a),as)
    case (ACons(a, as), ANil) => widen_AList(AMany(a),as)
    case (AMany(a), AMany(b)) => AMany(intervals.Lattice.widen(a,b))
    case (AMany(e), ACons(b, bs)) => widen_AList(AMany(intervals.Lattice.widen(e,b)), bs)
    case (ACons(a,as), AMany(e)) => widen_AList(AMany(intervals.Lattice.widen(a,e)), as)
    case (ACons(a,as), ACons(b, bs)) => ACons(intervals.Lattice.widen(a,b), widen_AList(as,bs))
  }

  def widen_AOption(ao1: AOption[AInt], ao2: AOption[AInt]) : AOption[AInt] = (ao1, ao2) match {
    case (ANone, ANone) => ANone
    case (ANone, ASome(e)) => AMaybe(e)
    case (ASome(e), ANone) => AMaybe(e)
    case (ANone, AMaybe(e)) => AMaybe(e)
    case (AMaybe(e), ANone) => AMaybe(e)
    case (AMaybe(a), AMaybe(b)) => AMaybe(intervals.Lattice.widen(a,b))
    case (ASome(a), ASome(b)) => ASome(intervals.Lattice.widen(a,b))
    case (AMaybe(a), ASome(b)) => AMaybe(intervals.Lattice.widen(a,b))
    case (ASome(a), AMaybe(b)) => AMaybe(intervals.Lattice.widen(a,b))
  }

}




/*
//TODO aTail -> returns  AOption[AInt] or AOption[AList]
//Here: which interval describes tail
def aTail(l: AList): AOption[AInt] = l match {
  case ANil  => ANone
  case ACons(_, ANil) => ANone
  case ACons(_, ACons(h,t)) => widen_AOption(ASome(h), aTail(t))
  case ACons(_, AMany(e)) =>AMaybe(e)
  case AMany(e) => AMaybe(e) //AMany = ANil ≀ ACons(e, Many(e))
  }
*/




