function PetList({ pets }) {
    return (
        <div>
            <h3>Mina djur</h3>
            {pets.length === 0 ? <p>Inga djur hittades.</p> : (
                <ul>
                    {pets.map(pet => (
                        <li key={pet.id}>{pet.name} ({pet.species})</li>
                    ))}
                </ul>
            )}
        </div>
    );
}

export default PetList;