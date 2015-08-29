Require Coq.Lists.List.

Definition subtract
  (p0 p1 : path_virtual)
  (_     : is_ancestor_of p1 p0 \/ p0 = p1) :=
  List.skipn (length p1) p0.
