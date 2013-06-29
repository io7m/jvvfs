Axiom archive_lookup_file_ancestor : forall
  (a   : archive)
  (p q : path_virtual),
  is_ancestor_of p q ->
    exists r, archive_lookup a p = Success _ _ (Some r) /\ file_reference_type r = File ->
      archive_lookup a q = Failure _ _ FSErrorNotADirectory.
