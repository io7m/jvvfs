Require Coq.Arith.Peano_dec.

Axiom ancestor_or_equal : forall (p q : path_virtual),
  {is_ancestor_of p q \/ q = p}+{~is_ancestor_of p q \/ q = p}.

Fixpoint filesystem_lookup_union'
  (archives : list archive)
  (p        : path_virtual)
  (checks   : nat)
: io error_code (option file_reference) :=
  match archives with
  | nil         => Success _ _ None
  | cons a rest =>
    let m := archive_mount a in
      match ancestor_or_equal m p with
      | right _ => filesystem_lookup_union' rest p checks
      | left  H =>
        match archive_lookup a (subtract p m H) with
        | Success None                 => filesystem_lookup_union' rest p (S checks)
        | Success (Some r)             => Success _ _ (Some r)
        | Failure FSErrorNotADirectory =>
          if Peano_dec.eq_nat_dec checks 0
          then Failure _ _ FSErrorNotADirectory
          else Success _ _ None
        | Failure e => Failure _ _ e
        end
      end
  end.

Definition filesystem_lookup_union
  (archives : list archive)
  (p        : path_virtual)
:= filesystem_lookup_union' archives p 0.
