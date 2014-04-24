Require Coq.Arith.Peano_dec.
Require Coq.Lists.ListSet.

Axiom name_eq_dec : forall (n m : name),
  {n = m}+{n <> m}.

Fixpoint filesystem_directory_list_union'
  (archives : list archive)
  (p        : path_virtual)
  (checks   : nat)
  (current  : ListSet.set name)
: io error_code (list name) :=
  match archives with
  | nil         => Success _ _ current
  | cons a rest =>
    let m := archive_mount a in
      match ancestor_or_equal m p with
      | right _ => filesystem_directory_list_union' rest p checks current
      | left  H =>
        match archive_directory_list a (subtract p m H) with
        | Success names                => filesystem_directory_list_union' rest p (S checks) (ListSet.set_union name_eq_dec current names)
        | Failure FSErrorNotADirectory =>
          if Peano_dec.eq_nat_dec checks 0
          then Failure _ _ FSErrorNotADirectory
          else Success _ _ current
        | Failure e => Failure _ _ e
        end
      end
  end.

Definition filesystem_directory_list_union
  (archives : list archive)
  (p        : path_virtual)
:= filesystem_directory_list_union' archives p 0 nil.

Definition filesystem_directory_list
  (archives : list archive)
  (p        : path_virtual)
: io error_code (list name) :=
  match filesystem_lookup archives p with
  | Success None                        => Failure _ _ FSErrorNotADirectory
  | Success (Some FSReferenceFile)      => Failure _ _ FSErrorNotADirectory
  | Success (Some FSReferenceDirectory) => filesystem_directory_list_union archives p
  | Failure e                           => Failure _ _ e
  end.
