let sect_start_re =
  Str.regexp "^Section \\(.*\\)\\.$"
let sect_end_re =
  Str.regexp "^End \\(.*\\)\\.$"

type section = {
  section_name : string;
  section_out  : out_channel
}

let section_stack : (section list) ref =
  ref []

let section_new name =
  { section_name = name;
    section_out  = open_out (name ^ ".v") }

let section_close s =
  flush s.section_out;
  close_out s.section_out

let section_start name =
  begin
    prerr_endline ("Started " ^ name);
    section_stack := (section_new name) :: !section_stack
  end

let section_end name =
  begin
    prerr_endline ("Ended " ^ name);
    begin match !section_stack with
    | []        -> assert false
    | s :: rest ->
      begin
        section_close s;
        section_stack := rest
      end
    end
  end

let section_write line =
  begin match !section_stack with
  | []     -> ()
  | s :: _ ->
    begin
      output_string s.section_out line;
      output_string s.section_out "\n"
    end
  end

let main _ =
  begin try
    while true do
      let line = read_line () in
        if (Str.string_match sect_start_re line 0)
        then section_start (Str.matched_group 1 line)
        else if (Str.string_match sect_end_re line 0)
          then section_end (Str.matched_group 1 line)
          else section_write line
    done
  with End_of_file ->
    ()
  end

let _ = main ()

