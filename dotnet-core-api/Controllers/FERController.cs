using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using FERApi.Models;
using System;
using Microsoft.AspNetCore.Http;
using Microsoft.Azure.CognitiveServices.Vision.Face;
using System.IO;
using Microsoft.Azure.CognitiveServices.Vision.Face.Models;

namespace FERApi.Controllers
{
    [Route("api/[controller]")] 
    [ApiController]
    public class FERController : Controller
    {
        private readonly FERContext _context;

        public FERController()
        {
            _context = new FERContext();
        }


        [HttpPost]
        [ProducesResponseType(StatusCodes.Status200OK)]
        public async Task<ActionResult<FERItem>> PostFERItem(FERItem ferItem)
        {
            await _context.PostFERItem(ferItem);
            return Ok();
        }


    }
}
