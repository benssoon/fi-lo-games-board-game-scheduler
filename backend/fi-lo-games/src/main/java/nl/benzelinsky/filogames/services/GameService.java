package nl.benzelinsky.filogames.services;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import nl.benzelinsky.filogames.dtos.GameInputDto;
import nl.benzelinsky.filogames.dtos.GameOutputDto;
import nl.benzelinsky.filogames.dtos.GameStatsDto;
import nl.benzelinsky.filogames.exceptions.HasActiveEventsException;
import nl.benzelinsky.filogames.exceptions.RecordNotFoundException;
import nl.benzelinsky.filogames.mappers.GameMapper;
import nl.benzelinsky.filogames.mappers.GameStatsMapper;
import nl.benzelinsky.filogames.models.Game;
import nl.benzelinsky.filogames.models.GameStats;
import nl.benzelinsky.filogames.repositories.GameRepository;
import nl.benzelinsky.filogames.repositories.GameStatsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final EventService eventService;
    private final GameStatsRepository statsRepository;

    public GameService(GameRepository gameRepository, EventService eventService, GameStatsRepository statsRepository) {
        this.gameRepository = gameRepository;
        this.eventService = eventService;
        this.statsRepository = statsRepository;
    }

    /****** CRUD operations ******/

    // Create a game
    public GameOutputDto createGame(GameInputDto dtoIn) {
        Game game = GameMapper.toEntity(dtoIn);
        GameStats stats = new GameStats(game);
        game.setStats(stats);
        stats.setGame(game);
        this.gameRepository.save(game);
        return GameMapper.toOutputDto(game);
    }

    // Get game by ID
    public GameOutputDto getGameById(Long id) {
        return GameMapper.toOutputDto(
                this.gameRepository.findById(id)
                        .orElseThrow(() ->
                                new RecordNotFoundException("Game not found with id: " + id)));

    }

    // Get all games, optional title criterium
    public List<GameOutputDto> getAllGames(String title) {
        List<GameOutputDto> allGames = new ArrayList<>();
        if (title != null) {
            // Find and sort all games based on how close the title is to the given search term.
            FuzzySearch.extractSorted(title, this.gameRepository.findAll(), Game::getTitle, 68)
                    .stream()
                    //.filter(result -> result.getScore() >= 60) // Optional, for returning only results with a score higher than 60
                    .map(BoundExtractedResult::getReferent)
                    .toList()
                    .forEach(game ->
                            allGames.add(GameMapper.toOutputDto(game)));
        }
        else {
            this.gameRepository.findAll()
                    .forEach(game ->
                            allGames.add(GameMapper.toOutputDto(game)));
        }
        return allGames;
    }

    // Update game by ID
    public GameOutputDto updateGameById(Long id, GameInputDto dtoIn) {
        Game toUpdate = this.gameRepository.findById(id)
                .orElseThrow(() ->
                        new RecordNotFoundException("Game not found with id: " + id));

        toUpdate.setTitle(dtoIn.title);
        toUpdate.setDescription(dtoIn.description);
        toUpdate.setMinPlayers(dtoIn.minPlayers);
        toUpdate.setMaxPlayers(dtoIn.maxPlayers);
        toUpdate.setComplexity(dtoIn.complexity);
        toUpdate.setMinAge(dtoIn.minAge);
        toUpdate.setMaxAge(dtoIn.maxAge);

        this.gameRepository.save(toUpdate);

        return GameMapper.toOutputDto(toUpdate);
    }

    // Delete game by ID
    public String deleteGameById(Long id) {
        Game toDelete = this.gameRepository.findById(id)
                .orElseThrow(() ->
                        new RecordNotFoundException("Game", id));
        if (!toDelete.getActiveEvents().isEmpty()) {
            List<String> events = new ArrayList<>();
            Map<Long, String> eventIds = new HashMap<>();
            toDelete.getActiveEvents()
                    .forEach(event -> {
                        events.add(event.getName());
                        eventIds.put(event.getId(), event.getName());
                    });
            throw new HasActiveEventsException(id, eventIds);
        }
        else {
            this.gameRepository.deleteById(id);
        }
        return "Game " + toDelete.getTitle() + " has been deleted.";
    }

    // Delete game by ID including associated events
    public String deleteGameAndEvents(Long id) {
        Game toDelete = this.gameRepository.findById(id)
                .orElseThrow(() ->
                        new RecordNotFoundException("Game", id));
        List<String> messages = new ArrayList<>();
        List<String> eventNames = new ArrayList<>();
        toDelete.getActiveEvents()
                .forEach(event ->
                        eventNames.add(event.getName()));
        // Iterate over a copy of toDelete.activeEvents to avoid a bug when deleting events from the same collection we're iterating over.
        new ArrayList<>(toDelete.getActiveEvents())
                .forEach(event ->
                        messages.add(this.eventService.deleteEventById(event.getId())));
        this.gameRepository.delete(toDelete);
        return "Deleting " + toDelete.getTitle() + " and its events:\n" +
                "    " + eventNames.toString() + "\n" +
                String.join("\n", messages) +
                "\nGame with id " + toDelete.getId() + " has been deleted.";
    }

    // Delete all Games
    public String deleteAllGames() {
        Long protectedId = 1L;
        List<Long> toDeleteIds = new ArrayList<>();
        // Create a list of all IDs except for the first one.
        this.gameRepository
                .findAll()
                .forEach(game ->
                        toDeleteIds.add(game.getId()));
        toDeleteIds.remove(protectedId); // Remove ID 1 from the list of all IDs.

        toDeleteIds
                .forEach(id ->
                        System.out.println("\n"+this.deleteGameById(id)+"\n"));
        return "All games except Game with id " + protectedId + " have been deleted.";
    }

    // Get stats
    public GameStatsDto getStats(Long gameId) {
        Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new RecordNotFoundException("Game", gameId));
        return GameStatsMapper.toDto(game.getStats());
    }
}
