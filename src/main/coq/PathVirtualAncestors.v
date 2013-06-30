Require Coq.Lists.List.

Fixpoint ancestors_including_self (p : path_virtual) : list path_virtual :=
  cons root (match p with
    | nil       => nil
    | cons y ys => List.map (cons y) (ancestors_including_self ys)
    end).

Definition ancestors (p : path_virtual) : list path_virtual :=
  List.removelast (ancestors_including_self p).
