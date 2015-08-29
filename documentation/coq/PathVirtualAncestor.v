Require Coq.Lists.List.

Definition is_prefix (p0 p1 : path_virtual) :=
  List.firstn (length p0) p1 = p0.

Definition is_ancestor_of (p0 p1 : path_virtual) :=
  p0 <> p1 /\ is_prefix p0 p1.
