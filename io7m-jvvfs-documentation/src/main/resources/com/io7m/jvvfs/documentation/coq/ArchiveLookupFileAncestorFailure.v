Axiom archive_lookup_file_ancestor : forall
  (a   : archive)
  (p q : path_virtual),
  is_ancestor_of p q ->
    archive_lookup a p = Success _ _ (Some FSReferenceFile) ->
      archive_lookup a q = Failure _ _ FSErrorNotADirectory.
