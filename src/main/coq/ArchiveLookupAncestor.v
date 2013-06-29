Axiom archive_lookup_ancestor : forall
  (a   : archive)
  (p q : path_virtual),
  is_ancestor_of p q ->
    exists r, archive_lookup a q = Success _ _ (Some r) ->
      exists s, archive_lookup a p = Success _ _ (Some s) /\ file_reference_type s = Directory.
