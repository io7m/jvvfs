Axiom archive_directory_list_file : forall
  (a : archive)
  (p : path_virtual),
  exists r, archive_lookup a p = Success _ _ (Some r) /\ file_reference_type r = File ->
    archive_directory_list a p = Failure _ _ FSErrorNotADirectory.
