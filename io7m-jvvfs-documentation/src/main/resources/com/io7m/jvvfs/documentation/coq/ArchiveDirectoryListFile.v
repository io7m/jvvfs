Axiom archive_directory_list_file : forall
  (a : archive)
  (p : path_virtual),
  archive_lookup a p = Success _ _ (Some FSReferenceFile) ->
    archive_directory_list a p = Failure _ _ FSErrorNotADirectory.
