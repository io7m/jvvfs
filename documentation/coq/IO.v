Inductive io (F S : Type) : Type :=
  | Success : S -> io F S
  | Failure : F -> io F S.
