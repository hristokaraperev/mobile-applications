-- CIQUAL foods often lack an energy value; relax the NOT NULL constraint.
ALTER TABLE foods ALTER COLUMN energy_kcal DROP NOT NULL;
