# measuring_polyphony_metadata_script
Follow these steps to insert metadata into MEI files:
1) Go to the spreadsheet in google sheets and export the file as tsv, save it as data.tsv (NOTE: make sure the columns are in the following order, otherwise the locations of data in the metadata will be switched up: [Short_Title, Composer, Genre, Manuscript_source, Folio_numbers, Number_of_voices, Clefs, Concordant_Sources, Primary Edition, Reference, MEI_CMN_file, MEI_MENS_file, PDF_file, Mp3_file, Has_online_images, IIIF_manifest, DIAMM_composition, DIAMM_source, Other_online_images, Transcription_entered_by, Transcription_entered_date, Transcription_checked_by, Transcription_checked_by_date, Additional_comments_on_transcription, Notes_on_motet_texts, Variants_description, quadruplum, triplum, motetus, tenor, contratenor]
2) Replace the 'data.tsv' in this program with the new one you just exported
3) Replace the folders in "newmei" with the ones in "old"
4) Run UpdateMei.java
5) All the files in newmei should be updated with the metadata.
