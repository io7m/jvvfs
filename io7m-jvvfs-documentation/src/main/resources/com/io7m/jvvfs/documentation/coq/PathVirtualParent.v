Require Coq.Lists.List.

Definition is_parent_of (p0 p1 : path_virtual) :=
  is_ancestor_of p0 p1 /\ exists n, app p0 (n :: nil) = p1.
