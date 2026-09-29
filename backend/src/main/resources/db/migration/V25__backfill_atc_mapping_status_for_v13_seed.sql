
UPDATE medicines
SET atc_mapping_status = 'AUTO_MATCHED'
WHERE keml_code IN (
                    'KEML-N02BE01', 'KEML-J01CA04', 'KEML-M01AE01', 'KEML-P01BF01',
                    'KEML-A10BA02', 'KEML-C08CA01', 'KEML-J01DD04', 'KEML-R03AC02',
                    'KEML-A07CA'
    )
  AND atc_code IS NOT NULL
  AND atc_mapping_status = 'UNMAPPED';
