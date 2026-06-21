CREATE TABLE event_performers (
    event_id UUID NOT NULL REFERENCES events(id),
    performer_id UUID NOT NULL REFERENCES performers(id),
    PRIMARY KEY (event_id, performer_id)
);
