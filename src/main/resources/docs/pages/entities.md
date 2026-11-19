# Entities

Data structures returned by the requests.

## No searchable entities

Split PDF defines **no `Entity` types** and registers nothing searchable. It holds no records of its
own: each call takes a document in, produces files, and keeps nothing afterwards, so there is
nothing for Krista to store, search or look up later.

## Split File record

[Split PDF By Page Count](pages/SplitPdfByPageCount.md) returns its results as **Split Files**, a
list of typed records rather than an entity. A record carries a real file alongside its metadata,
which the multi-field record form supports directly.

| Field | Type | Description |
|---|---|---|
| PartNumber | Number | Position of this file in the sequence, starting at 1. |
| FileName | Text | Name of the generated file as stored in Krista, e.g. `report_p21-40.pdf`. |
| StartPage | Number | First source page included in this file (1-based, inclusive). |
| EndPage | Number | Last source page included in this file (1-based, inclusive). |
| PageCount | Number | Pages in this file. Equals Pages Per Split for every file except, possibly, the last. |
| File | File | The generated PDF — downloadable, attachable, and usable as the input to another request. |

Each field is individually addressable in a workflow, so a loop over **Split Files** can read a
part's page range and pass its **File** straight into the next step.
