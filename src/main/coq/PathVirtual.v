Definition path_virtual :=
  list {n : name | name_valid n}.

Definition root : path_virtual :=
  nil.
