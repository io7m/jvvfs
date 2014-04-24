Require Coq.Arith.Peano_dec.

Fixpoint filesystem_lookup_typical
  (archives : list archive)
  (p        : path_virtual)
: io error_code (option file_reference) :=
  match archives with
  | nil         => Success _ _ None
  | cons a rest =>
    let m := archive_mount a in
      match ancestor_or_equal m p with
      | right _ => filesystem_lookup_typical rest p
      | left  H => archive_lookup a (subtract p m H)
      end    
  end.
